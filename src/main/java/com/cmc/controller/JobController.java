package com.cmc.controller;

import com.cmc.model.TimerInfo;
import com.cmc.jobs.BulkImportJob;
import com.cmc.service.SchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JobController {
    private final SchedulerService schedulerService;
    private final long REPEAT_INTERVAL = 86400000;
    private final int INITIAL_OFFSET = 1000;

    @Autowired
    public JobController(final SchedulerService scheduler) {
        this.schedulerService = scheduler;
    }

    public void runBulkImportJob(String importType) {
        final TimerInfo info = new TimerInfo();
        info.setRunForever(true);
        info.setRepeatIntervalMs(REPEAT_INTERVAL);
        info.setInitialOffsetMs(INITIAL_OFFSET);
        info.setCallbackData(importType);
        schedulerService.schedule(BulkImportJob.class, info);
    }
}
