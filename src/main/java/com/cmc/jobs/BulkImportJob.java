package com.cmc.jobs;

import com.cmc.dto.ResourceDTO;
import com.cmc.model.DbInfo;
import com.cmc.model.TimerInfo;
import com.cmc.service.bulkImport.BulkImportService;
import com.cmc.service.inputData.RedshiftClusterImportService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class BulkImportJob implements Job {

    private static final Logger LOG = LoggerFactory.getLogger(BulkImportJob.class);
    private final RedshiftClusterImportService redshiftClusterImportService;
    private final BulkImportService bulkImportService;

    @Autowired
    public BulkImportJob(RedshiftClusterImportService redshiftClusterImportService, BulkImportService bulkImportService) {
        this.redshiftClusterImportService = redshiftClusterImportService;
        this.bulkImportService = bulkImportService;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        String IMPORT_TYPE = "Redshift";
        JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();
        TimerInfo info = (TimerInfo) jobDataMap.get(BulkImportJob.class.getSimpleName());
        DbInfo dbInfo = info.getCallbackData();
        ResourceDTO resourceDTO = redshiftClusterImportService.getResources(dbInfo);
        String resp = null;
        try {
            resp = bulkImportService.bulkImport(resourceDTO, IMPORT_TYPE);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        LOG.info(resp);
    }
}