package com.cmc.controller;

import java.io.IOException;
import java.sql.*;

import com.cmc.service.ApiService;
import com.cmc.service.RedshiftClusterService;
import com.cmc.service.MoengageImportLogService;
import com.cmc.service.ExcelService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @Value("${secret.dbURL}")
    private String dbURL;

    @Value("${secret.masterUsername}")
    private String MasterUsername;

    @Value("${secret.masterUserPassword}")
    private String MasterUserPassword;

    @Value("${secret.google.geolocation.apikey}")
    private String apiKey;

    private RedshiftClusterService redshiftClusterService;
    private final ExcelService excelService;
    private final MoengageImportLogService moengageImportLogService;

    @Autowired
    public IndexController(ExcelService excelService, RedshiftClusterService redshiftClusterService, MoengageImportLogService moengageImportLogService, ApiService apiService) {
        this.excelService = excelService;
        this.redshiftClusterService = redshiftClusterService;
        this.moengageImportLogService = moengageImportLogService;
    }

    @GetMapping("/")
    public String init() {
        return "index";
    }

    @PostMapping("/import")
    public String importData(@RequestParam("file") MultipartFile excelFile, Model model) throws IOException {
        JSONObject mainBulkObj = excelService.importData(excelFile, apiKey);
        model.addAttribute("responseExcel", bulkImport(mainBulkObj));
        return "index";
    }

    @PostMapping("/import-redshift")
    public String importDataFromRedshift(Model model) throws SQLException, JsonProcessingException {
        JSONObject mainBulkObj = redshiftClusterService.importData(apiKey);
        model.addAttribute("responseRedshift", bulkImport(mainBulkObj));
        return "index";
    }

    private String bulkImport(JSONObject requestBody) throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper mapper = new ObjectMapper();
        String requestJson = mapper.writeValueAsString(requestBody.toMap());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(this.userName, this.password);

        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);
        String response;
        String importStatus;
        try {
            response = restTemplate.postForObject(this.url, entity, String.class);
            importStatus = "Successfully imported data";
            this.moengageImportLogService.addLog(importStatus, requestBody);
        } catch (RestClientException e) {
            System.out.println(e.getStackTrace());
            importStatus = "Data import not successful";
            this.moengageImportLogService.addLog(importStatus, requestBody);
            response = e.getMessage();
        }
        return response;
    }
}