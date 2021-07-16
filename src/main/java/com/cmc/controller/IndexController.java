package com.cmc.controller;

import java.io.IOException;
import java.sql.*;

import com.cmc.service.bulkImport.BulkImportService;
import com.cmc.service.inputData.ExcelFileImportService;
import com.cmc.service.inputData.RedshiftClusterImportService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class IndexController {

    @Value("${secret.google.geolocation.apikey}")
    private String apiKey;

    private String IMPORT_TYPE = "";
    private final ExcelFileImportService excelFileImportService;
    private final RedshiftClusterImportService redshiftClusterImportService;
    private final BulkImportService bulkImportService;

    @Autowired
    public IndexController(ExcelFileImportService excelFileImportService, RedshiftClusterImportService redshiftClusterImportService, BulkImportService bulkImportService) {
        this.excelFileImportService = excelFileImportService;
        this.redshiftClusterImportService = redshiftClusterImportService;
        this.bulkImportService = bulkImportService;
    }

    @GetMapping("/")
    public String init() {
        return "index";
    }

    @PostMapping("/import")
    @ResponseBody
    public String importData(@RequestParam("file") MultipartFile excelFile, Model model) throws IOException {
        IMPORT_TYPE = "Excel";
        JSONObject mainBulkObj = excelFileImportService.importData(excelFile, apiKey);
        model.addAttribute("responseExcel", bulkImportService.bulkImport(mainBulkObj, IMPORT_TYPE));
        return "index";
    }

    @PostMapping("/import-redshift")
    @ResponseBody
    public String importDataFromRedshift(Model model) throws SQLException, JsonProcessingException {
        IMPORT_TYPE = "Redshift";
        JSONObject mainBulkObj = redshiftClusterImportService.importData(apiKey);
        String resp = bulkImportService.bulkImport(mainBulkObj, IMPORT_TYPE);
        return resp;
    }

}