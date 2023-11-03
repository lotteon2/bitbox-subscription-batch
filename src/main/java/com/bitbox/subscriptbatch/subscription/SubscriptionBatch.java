package com.bitbox.subscriptbatch.subscription;

import com.bitbox.subscriptbatch.domain.Subscription;
import com.bitbox.subscriptbatch.repository.SubscriptionRepository;
import io.github.bitbox.bitbox.dto.NotificationDto;
import io.github.bitbox.bitbox.dto.SubscriptionExpireDto;
import io.github.bitbox.bitbox.enums.NotificationType;
import io.github.bitbox.bitbox.util.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

import javax.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Configuration
public class SubscriptionBatch {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final KafkaTemplate<String, SubscriptionExpireDto> subscriptionTemplate;
    private final KafkaTemplate<String, NotificationDto> notificationTemplate;
    private final EntityManagerFactory emf;
    private final SubscriptionRepository subscriptionRepository;
    @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
    private int chunkSize;
    @Value("${alarmTopicName}")
    private String alarmTopicName;
    @Value("${expirationTopicName}")
    private String expirationTopicName;
    private final NotificationType messageType = NotificationType.SUBSCRIPTION;

    // 1시간 단위로 도는 배치
    @Bean
    public Job subscriptionExpirationJob() {
        return jobBuilderFactory.get("subscriptionExpirationJob")
                .start(subscriptionExpirationStep())
                .build();
    }

    @Bean
    public Step subscriptionExpirationStep() {
        return stepBuilderFactory.get("subscriptionExpirationStep")
                .<Subscription, Subscription>chunk(chunkSize)
                .reader(subscriptionReader())
                .writer(subscriptionWriter(null))
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<Subscription> subscriptionReader() {
        Map<String, Object> parameterValues = new HashMap<>();
        parameterValues.put("isValid", true);

        return new JpaPagingItemReaderBuilder<Subscription>()
                .name("subscriptionReader")
                .entityManagerFactory(emf)
                .pageSize(chunkSize)
                .queryString("SELECT s FROM Subscription s WHERE s.isValid = :isValid ORDER BY s.subscriptionId ASC")
                .parameterValues(parameterValues)
                .build();
    }

    // --job.name=subscriptionExpirationJob date=20230928170024
    @Bean
    @StepScope
    public ItemWriter<Subscription> subscriptionWriter(@Value("#{jobParameters[date]}") String date) {
        LocalDateTime localDateTime = DateTimeUtil.convertTimeFormat(date);
        return items -> {
            List<Long> subscriptionIds = new ArrayList<>();
            for (Subscription subscription : items) {
                switch(DateTimeUtil.compareTwoTime(localDateTime, subscription.getEndDate())){
                    case EXPIRED: // 만료
                        subscriptionIds.add(subscription.getSubscriptionId());
                        subscriptionTemplate.send(expirationTopicName, SubscriptionExpireDto.builder()
                                .startDate(subscription.getStartDate())
                                .endDate(subscription.getEndDate())
                                .memberId(subscription.getMemberId())
                                .build());
                        break;
                    case ONE_HOUR_LEFT: // 1시간전
                        notificationTemplate.send(alarmTopicName, NotificationDto.builder()
                                .notificationType(messageType)
                                .boardType(null)
                                .receiverId(subscription.getMemberId())
                                .boardId(null)
                                .senderNickname(null)
                                .build());
                        break;
                }
            }

            subscriptionRepository.updateSubscriptionStat(subscriptionIds);
        };
    }
}