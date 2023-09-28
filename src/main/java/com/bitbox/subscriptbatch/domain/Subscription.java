package com.bitbox.subscriptbatch.domain;

import io.github.bitbox.bitbox.enums.SubscriptionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name="subscription")
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscription_id")
    private Long subscriptionId;

    @Column(name="member_id", nullable = false)
    private String memberId;

    @Column(name="start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name="end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name="is_valid", nullable = false)
    private boolean isValid;

    @Column(name="subscriptionType", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private SubscriptionType subscriptionType;
}