package com.bitbox.subscriptbatch;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class KafkaConsumerMock {
    private CountDownLatch latch = new CountDownLatch(1);
    private List<Object> alarmPayload = new ArrayList<>();
    private List<Object> expirationPayload = new ArrayList<>();

    @KafkaListener(topics = "${alarmTopicName}", groupId = "testConsumerGroup")
    public void receiveAlarm(ConsumerRecord<?, ?> consumerRecord) {
        alarmPayload.add(consumerRecord.value());
        latch.countDown();
    }

    @KafkaListener(topics = "${expirationTopicName}", groupId = "testConsumerGroup")
    public void receiveExpiration(ConsumerRecord<?, ?> consumerRecord) {
        expirationPayload.add(consumerRecord.value());
        latch.countDown();
    }

    public void resetLatch() {
        latch = new CountDownLatch(1);
    }

    public CountDownLatch getLatch() {
        return latch;
    }

    public List<Object> getAlarmPayload() {
        return alarmPayload;
    }

    public List<Object> getExpirationPayload() {
        return expirationPayload;
    }
}
