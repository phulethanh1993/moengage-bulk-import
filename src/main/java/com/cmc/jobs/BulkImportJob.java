package com.cmc.jobs;

import com.cmc.model.TimerInfo;
import com.cmc.service.BulkImportService;
import com.cmc.service.ExcelFileImportService;
import com.cmc.service.RedshiftClusterImportService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONObject;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

@Component
public class BulkImportJob implements Job {
    
    @Value("${secret.google.geolocation.apikey}")
    private String apiKey;
    
    private static final Logger LOG = LoggerFactory.getLogger(BulkImportJob.class);
    private final RedshiftClusterImportService redshiftClusterImportService;
    private final ExcelFileImportService excelFileImportService;
    private final BulkImportService bulkImportService;

    @Autowired
    public BulkImportJob(RedshiftClusterImportService redshiftClusterImportService, ExcelFileImportService excelFileImportService, BulkImportService bulkImportService) {
        this.redshiftClusterImportService = redshiftClusterImportService;
        this.excelFileImportService = excelFileImportService;
        this.bulkImportService = bulkImportService;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();
        TimerInfo info = (TimerInfo) jobDataMap.get(BulkImportJob.class.getSimpleName());
        String importType = info.getCallbackData();
        JSONObject mainBulkObj = new JSONObject();
        String resp = null;
        try {
            mainBulkObj = redshiftClusterImportService.importData(apiKey);
            resp = bulkImportService.bulkImport(mainBulkObj, importType);
        } catch (SQLException | JsonProcessingException throwables) {
            throwables.printStackTrace();
        }

        LOG.info("Successfully imported data: " + resp);
    }
}
