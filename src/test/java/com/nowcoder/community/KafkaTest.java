package com.nowcoder.community;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class KafkaTest {
    @Autowired
    private KafkaProducer kafkaProducer;

    @Test
    void testKafka() throws InterruptedException {
        kafkaProducer.sendMessage("test", "Hello");
        kafkaProducer.sendMessage("test", "World");
        Thread.sleep(10000);
    }
}

@Component
class KafkaProducer {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(String topic, String content) {
        kafkaTemplate.send(topic, content);
    }
}

@Component
class KafkaConsumer {

    @KafkaListener(topics = {"test"})
    public void handleMessage(ConsumerRecord<String, String> record) {
        System.out.println(record.value());
    }
}
