package com.jacent.storefront.scheduler;

import com.jacent.storefront.service.ItemService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class DailyScheduler {

    private final ItemService itemService;
    DailyScheduler(ItemService itemService){
        this.itemService = itemService;
    }

    @Scheduled(cron = "0 30 0 * * ?", zone = "America/Chicago")
    public void runDailyTask() {
        LocalDateTime startTime = LocalDateTime.now();
        System.out.println("Daily job started at: " + LocalDateTime.now());
        try {
            this.itemService.rebuildOpenSearchIndexForItems();
        } finally {
            LocalDateTime endTime = LocalDateTime.now();
            Duration duration = Duration.between(startTime, endTime);
            System.out.println("Job ended at:    " + endTime);
            System.out.println("Time taken:      " + duration.toSeconds() + "s "
                    + duration.toMillisPart() + "ms");
        }
    }
}