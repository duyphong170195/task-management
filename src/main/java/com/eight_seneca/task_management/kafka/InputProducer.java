package com.eight_seneca.task_management.kafka;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class InputProducer {

    public static void main(String[] args) throws Exception {
        String bootstrap = env("BOOTSTRAP", "localhost:9092");
        String topic = env("TOPIC", "in");

        Properties p = new Properties();
        p.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        p.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        p.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        // Not required for EOS demo, but fine to have
        p.put(ProducerConfig.ACKS_CONFIG, "all");

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(p)) {
            for (int i = 1; i <= 10000; i++) {
                String key = "k" + (i % 3);
                String value = "msg-" + i;
                ProducerRecord<String, String> rec = new ProducerRecord<>(topic, key, value);

                producer.send(rec, (md, ex) -> {
                    if (ex != null) ex.printStackTrace();
                    else System.out.printf("IN  -> topic=%s partition=%d offset=%d key=%s value=%s%n",
                            md.topic(), md.partition(), md.offset(), key, value);
                });

                TimeUnit.MILLISECONDS.sleep(80);
            }
            producer.flush();
        }
    }

    static String env(String k, String def) {
        String v = System.getenv(k);
        return (v == null || v.isBlank()) ? def : v;
    }
}
