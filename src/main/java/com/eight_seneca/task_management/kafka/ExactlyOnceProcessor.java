package com.eight_seneca.task_management.kafka;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.ProducerFencedException;
import org.apache.kafka.common.errors.OutOfOrderSequenceException;
import org.apache.kafka.common.errors.AuthorizationException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Duration;
import java.util.*;

public class ExactlyOnceProcessor {

    public static void main(String[] args) {
        String bootstrap = env("BOOTSTRAP", "localhost:9092");
        String inTopic = env("IN_TOPIC", "in");
        String outTopic = env("OUT_TOPIC", "out");
        String groupId = env("GROUP_ID", "eos-processor-group");
        String transactionalId = env("TX_ID", "eos-processor-1"); // must be stable across restarts

        // ---- Consumer ----
        Properties cprops = new Properties();
        cprops.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        cprops.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        cprops.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        cprops.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        cprops.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        cprops.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        // When reading transactional topics, use read_committed
        cprops.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        cprops.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "200");

        // ---- Producer (idempotent + transactional) ----
        Properties pprops = new Properties();
        pprops.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        pprops.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        pprops.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        pprops.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        pprops.put(ProducerConfig.ACKS_CONFIG, "all");
        pprops.put(ProducerConfig.RETRIES_CONFIG, Integer.toString(Integer.MAX_VALUE));
        // keep <= 5 for idempotent producer safety (Kafka default is fine)
        pprops.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "5");

        pprops.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, transactionalId);

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(cprops);
             KafkaProducer<String, String> producer = new KafkaProducer<>(pprops)) {

            producer.initTransactions();
            consumer.subscribe(List.of(inTopic));

            System.out.println("EOS Processor started. Kill it anytime; restart should not duplicate committed output.");

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
                if (records.isEmpty()) continue;

                producer.beginTransaction();
                try {
                    // 1) produce derived output
                    for (ConsumerRecord<String, String> r : records) {
                        String outValue = "processed(" + r.topic() + "-" + r.partition() + "@" + r.offset() + "):" + r.value();
                        producer.send(new ProducerRecord<>(outTopic, r.key(), outValue));
                    }

                    // 2) commit offsets as part of the SAME transaction
                    Map<TopicPartition, OffsetAndMetadata> offsets = nextOffsets(records);
                    producer.sendOffsetsToTransaction(offsets, consumer.groupMetadata());

                    // 3) commit the transaction -> outputs become visible + offsets committed
                    producer.commitTransaction();

                    System.out.printf("Committed txn: wrote=%d, committed offsets=%s%n",
                            records.count(), offsets);

                } catch (ProducerFencedException | OutOfOrderSequenceException | AuthorizationException fatal) {
                    // These are fatal for the producer instance
                    System.err.println("Fatal producer error (fenced/out-of-order/auth). Exiting.");
                    fatal.printStackTrace();
                    break;
                } catch (Exception e) {
                    // Abort: output not visible to read_committed; offsets not committed
                    System.err.println("Error -> abortTransaction (will reprocess input).");
                    e.printStackTrace();
                    producer.abortTransaction();
                }
            }
        }
    }

    static Map<TopicPartition, OffsetAndMetadata> nextOffsets(ConsumerRecords<String, String> records) {
        Map<TopicPartition, OffsetAndMetadata> out = new HashMap<>();
        for (TopicPartition tp : records.partitions()) {
            List<ConsumerRecord<String, String>> part = records.records(tp);
            long nextOffset = part.get(part.size() - 1).offset() + 1;
            out.put(tp, new OffsetAndMetadata(nextOffset));
        }
        return out;
    }

    static String env(String k, String def) {
        String v = System.getenv(k);
        return (v == null || v.isBlank()) ? def : v;
    }
}
