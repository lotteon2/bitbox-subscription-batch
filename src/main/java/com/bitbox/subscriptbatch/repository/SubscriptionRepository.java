package com.bitbox.subscriptbatch.repository;

import com.bitbox.subscriptbatch.domain.Subscription;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubscriptionRepository extends CrudRepository<Subscription, Long> {
    @Modifying
    @Query("update Subscription s set s.isValid = false where s.subscriptionId IN :subscriptionIds")
    void updateSubscriptionStat(@Param("subscriptionIds") List<Long> subscriptionIds);
}