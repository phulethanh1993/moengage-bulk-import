package com.cmc.controller;

import java.io.IOException;
import java.sql.*;

import com.cmc.service.ApiService;
import com.cmc.service.RedshiftClusterImportService;
import com.cmc.service.MoengageImportLogService;
import com.cmc.service.ExcelFileImportService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
public class IndexController {

    @Value("${bulk.api.url}")
    private String url;

    @Value("${app.id}")
    private String userName;

    @Value("${secret.key}")
    private String password;

    @Value("${secret.google.geolocation.apikey}")
    private String apiKey;

    private RedshiftClusterImportService redshiftClusterImportService;
    private final ExcelFileImportService excelFileImportService;
    private final MoengageImportLogService moengageImportLogService;
    private String IMPORT_TYPE = "";

    @Autowired
    public IndexController(ExcelFileImportService excelFileImportService, RedshiftClusterImportService redshiftClusterImportService, MoengageImportLogService moengageImportLogService, ApiService apiService) {
        this.excelFileImportService = excelFileImportService;
        this.redshiftClusterImportService = redshiftClusterImportService;
        this.moengageImportLogService = moengageImportLogService;
    }

    @GetMapping("/")
    public String init() {
        return "index";
    }

    @PostMapping("/import")
    public String importData(@RequestParam("file") MultipartFile excelFile, Model model) throws IOException {
        IMPORT_TYPE = "Excel";
        JSONObject mainBulkObj = excelFileImportService.importData(excelFile, apiKey);
        model.addAttribute("responseExcel", bulkImport(mainBulkObj, IMPORT_TYPE));
        return "index";
    }

    @PostMapping("/import-redshift")
    public String importDataFromRedshift(Model model) throws SQLException, JsonProcessingException {
        IMPORT_TYPE = "Redshift";
        JSONObject mainBulkObj = redshiftClusterImportService.importData(apiKey);
        model.addAttribute("responseRedshift", bulkImport(mainBulkObj, IMPORT_TYPE));
        return "index";
    }

    private String bulkImport(JSONObject requestBody, String importType) throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper mapper = new ObjectMapper();
        String requestJson = mapper.writeValueAsString(requestBody.toMap());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(this.userName, this.password);

        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);
        String response = null;
        String importStatus;
        try {
            response = restTemplate.postForObject(this.url, entity, String.class);
            importStatus = "Successfully imported data from " + importType;
            this.moengageImportLogService.addLog(importStatus, requestBody);
        } catch (HttpClientErrorException e) {
            System.out.println(e.getStackTrace());
            if (requestBody.getJSONArray("elements").length() == 0) {
                importStatus = "Error. Data import cannot be empty.";
            } else {
                importStatus = "Data import not successful, please check the database connectivity.";
            }
            this.moengageImportLogService.addLog(importStatus, requestBody);
            HttpStatus status = e.getStatusCode();
            if (status == HttpStatus.BAD_REQUEST) {
                response = "Bad request. Data to import is either empty or the database connectivity is incorrect. Detail: " + e.getResponseBodyAsString();;
            }
        }
        return response;
    }
}