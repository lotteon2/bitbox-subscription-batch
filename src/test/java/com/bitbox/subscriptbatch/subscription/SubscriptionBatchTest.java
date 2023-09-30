package com.bitbox.subscriptbatch.subscription;

import com.bitbox.subscriptbatch.KafkaConsumerMock;
import com.bitbox.subscriptbatch.TestBatchConfig;
import com.bitbox.subscriptbatch.domain.Subscription;
import com.bitbox.subscriptbatch.repository.SubscriptionRepository;
import io.github.bitbox.bitbox.enums.SubscriptionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBatchTest
@SpringBootTest(classes = {SubscriptionBatch.class, TestBatchConfig.class, KafkaConsumerMock.class})
@EmbeddedKafka( partitions = 1,
        brokerProperties = { "listeners=PLAINTEXT://localhost:7777"},
        ports = {7777})
public class SubscriptionBatchTest {
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    @Autowired
    private KafkaConsumerMock kafkaConsumer;

    @BeforeEach
    public void insertData(){
        Subscription subscription1 = new Subscription(1L, "최성훈", stringToLocalDateTime("2023-09-25 18:00:00"),stringToLocalDateTime("2023-09-26 18:00:00"), true, SubscriptionType.ONE_DAY);
        Subscription subscription2 = new Subscription(2L, "최성훈", stringToLocalDateTime("2023-09-25 18:00:00"),stringToLocalDateTime("2023-09-28 18:00:00"), true, SubscriptionType.THREE_DAYS);
        Subscription subscription3 = new Subscription(3L, "최성훈", stringToLocalDateTime("2023-09-25 18:00:00"),stringToLocalDateTime("2023-10-02 18:00:00"), true, SubscriptionType.SEVEN_DAYS);
        Subscription subscription4 = new Subscription(4L, "최성훈", stringToLocalDateTime("2023-09-25 18:00:00"),stringToLocalDateTime("2023-09-26 18:00:00"), true, SubscriptionType.ONE_DAY);

        subscriptionRepository.save(subscription1);
        subscriptionRepository.save(subscription2);
        subscriptionRepository.save(subscription3);
        subscriptionRepository.save(subscription4);
    }

    @Test
    public void subscription테이블에서_isValid가_false인게_2개가존재하고_알림카프카통에는_1개가있고_채팅카프카통에는_2개가있음을_확인할수있다() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("date", "20230928170024")
                .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        kafkaConsumer.resetLatch(); // latch 초기화
        kafkaConsumer.getLatch().await(10, TimeUnit.SECONDS);

        List<Subscription> list = (List<Subscription>) subscriptionRepository.findAll();
        long validFalseCnt = list.stream()
                .filter(subscription -> !subscription.isValid())
                .count();

        assertEquals(kafkaConsumer.getAlarmPayload().size(),1);
        assertEquals(kafkaConsumer.getExpirationPayload().size(),2);
        assertEquals(validFalseCnt, 2);
    }


    public LocalDateTime stringToLocalDateTime(String dateString){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(dateString, formatter);
    }
}
