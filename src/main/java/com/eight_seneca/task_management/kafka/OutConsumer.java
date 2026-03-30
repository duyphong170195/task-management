package com.eight_seneca.task_management.kafka;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

public class OutConsumer {

    public static void main(String[] args) {
        String bootstrap = env("BOOTSTRAP", "localhost:9092");
        String topic = env("TOPIC", "out");

        Properties c = new Properties();
        c.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        c.put(ConsumerConfig.GROUP_ID_CONFIG, "out-reader");
        c.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        c.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        c.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        // IMPORTANT: only see committed transactional records
        c.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        c.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(c)) {
            consumer.subscribe(List.of(topic));
            System.out.println("OutConsumer started (read_committed).");

            while (true) {
                ConsumerRecords<String, String> recs = consumer.poll(Duration.ofMillis(500));
                for (ConsumerRecord<String, String> r : recs) {
                    System.out.printf("OUT <- partition=%d offset=%d key=%s value=%s%n",
                            r.partition(), r.offset(), r.key(), r.value());
                }
            }
        }
    }

    static String env(String k, String def) {
        String v = System.getenv(k);
        return (v == null || v.isBlank()) ? def : v;
    }
}
