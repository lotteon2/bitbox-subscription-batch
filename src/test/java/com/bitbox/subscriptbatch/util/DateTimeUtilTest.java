package com.bitbox.subscriptbatch.util;

import com.bitbox.subscriptbatch.domain.Subscription;
import io.github.bitbox.bitbox.enums.SubscriptionStatus;
import io.github.bitbox.bitbox.enums.SubscriptionType;
import io.github.bitbox.bitbox.util.DateTimeUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

class DateTimeUtilTest {
    private final String dateFormat = "yyyyMMddHHmmss";
    private final String testTime = "20230927175753";
    Subscription subscription1; // 만료
    Subscription subscription2; // 만료
    Subscription subscription3; // 1시간전
    Subscription subscription4; // 그외

    @BeforeEach
    public void insertData(){
        subscription1 = new Subscription(1L, "csh",
                LocalDateTime.parse("20230926170000", DateTimeFormatter.ofPattern(dateFormat)),
                LocalDateTime.parse("20230927170000", DateTimeFormatter.ofPattern(dateFormat)),
                true,
                SubscriptionType.ONE_DAY
        );
        subscription2 = new Subscription(2L, "csh",
                LocalDateTime.parse("20230920170000", DateTimeFormatter.ofPattern(dateFormat)),
                LocalDateTime.parse("20230923170000", DateTimeFormatter.ofPattern(dateFormat)),
                true,
                SubscriptionType.THREE_DAYS
        );
        subscription3 = new Subscription(3L, "csh",
                LocalDateTime.parse("20230926180000", DateTimeFormatter.ofPattern(dateFormat)),
                LocalDateTime.parse("20230927180000", DateTimeFormatter.ofPattern(dateFormat)),
                true,
                SubscriptionType.ONE_DAY
        );
        subscription4 = new Subscription(4L, "csh",
                LocalDateTime.parse("20230926190000", DateTimeFormatter.ofPattern(dateFormat)),
                LocalDateTime.parse("20230927190000", DateTimeFormatter.ofPattern(dateFormat)),
                true,
                SubscriptionType.ONE_DAY
        );
    }

    @Test
    public void 유저만료시간이_현재시간보다_이후라면_EXPIRED로_값이리턴되야한다(){
        Assertions.assertEquals(DateTimeUtil.compareTwoTime(DateTimeUtil.convertTimeFormat(testTime), subscription1.getEndDate()), SubscriptionStatus.EXPIRED);
        Assertions.assertEquals(DateTimeUtil.compareTwoTime(DateTimeUtil.convertTimeFormat(testTime), subscription2.getEndDate()), SubscriptionStatus.EXPIRED);
    }

    @Test
    public void 유저만료시간이_현재시간과_1시간차이가_있으면_ONE_HOUR_LEFT로_값이리턴되야한다(){
        Assertions.assertEquals(DateTimeUtil.compareTwoTime(DateTimeUtil.convertTimeFormat(testTime), subscription3.getEndDate()), SubscriptionStatus.ONE_HOUR_LEFT);
    }

    @Test
    public void 위의_케이스가_아니라면_OTHER로_값이리턴되야한다(){
        Assertions.assertEquals(DateTimeUtil.compareTwoTime(DateTimeUtil.convertTimeFormat(testTime), subscription4.getEndDate()), SubscriptionStatus.OTHER);
    }

    @Test
    public void 유저만료시간이_현재시간과_1시간차이가있으므로_EXPIRED로_값이리턴되야한다(){
        Subscription subscription5 = new Subscription(5L, "csh",
                LocalDateTime.parse("20230925180000", DateTimeFormatter.ofPattern(dateFormat)),
                LocalDateTime.parse("20230928180000", DateTimeFormatter.ofPattern(dateFormat)),
                true,
                SubscriptionType.THREE_DAYS
        );
        Assertions.assertEquals(DateTimeUtil.compareTwoTime(DateTimeUtil.convertTimeFormat("20230928170000"), subscription5.getEndDate()), SubscriptionStatus.ONE_HOUR_LEFT);
    }
}