package com.cmc.controller;

import java.io.IOException;
import java.sql.*;

import com.cmc.service.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class IndexController {

    @Value("${secret.google.geolocation.apikey}")
    private String apiKey;

    private final JobController jobController;
    private String IMPORT_TYPE = "";
    private final ExcelFileImportService excelFileImportService;
    private final BulkImportService bulkImportService;

    @Autowired
    public IndexController(JobController jobController, ExcelFileImportService excelFileImportService, BulkImportService bulkImportService) {
        this.jobController = jobController;
        this.excelFileImportService = excelFileImportService;
        this.bulkImportService = bulkImportService;
    }

    @GetMapping("/")
    public String init() {
        return "index";
    }

    @PostMapping("/import")
    public String importData(@RequestParam("file") MultipartFile excelFile, Model model) throws IOException {
        IMPORT_TYPE = "Excel";
        JSONObject mainBulkObj = excelFileImportService.importData(excelFile, apiKey);
        model.addAttribute("responseExcel", bulkImportService.bulkImport(mainBulkObj, IMPORT_TYPE));
        return "index";
    }

    @PostMapping("/import-redshift")
    public String importDataFromRedshift(Model model) throws SQLException {
        IMPORT_TYPE = "Redshift";
        jobController.runBulkImportJob(IMPORT_TYPE);
        model.addAttribute("responseRedshift");
        return "index";
    }

}