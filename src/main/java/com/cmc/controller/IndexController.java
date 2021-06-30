package com.cmc.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import com.cmc.service.ConnectToClusterService;
import com.cmc.service.LogService;
import com.cmc.service.ReadExcelService;
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

    private ConnectToClusterService connectToClusterService;
    private final ReadExcelService readExcelService;
    private final LogService logService;

    @Autowired
    public IndexController(ReadExcelService readExcelService, ConnectToClusterService connectToClusterService, LogService logService) {
        this.readExcelService = readExcelService;
        this.connectToClusterService = connectToClusterService;
        this.logService = logService;
    }

    @GetMapping("/")
    public String init() {
        return "index";
    }

    @PostMapping("/import")
    public String importData(@RequestParam("file") MultipartFile excelFile, Model model) throws IOException {
        JSONObject mainBulkObj = readExcelService.importData(excelFile);
        model.addAttribute("responseExcel", bulkImport(mainBulkObj));
        return "index";
    }

    @PostMapping("/import-redshift")
    public String importDataFromRedshift(Model model) {
        Connection conn = null;
        Statement stmt = null;
        try {
            Properties props = new Properties();
            props.setProperty("user", MasterUsername);
            props.setProperty("password", MasterUserPassword);
            conn = DriverManager.getConnection(dbURL, props);

            stmt = conn.createStatement();
            String sql = "select * from public.sbf_loan_portfolio;";
            ResultSet rs = stmt.executeQuery(sql);
            JSONObject mainBulkObj = connectToClusterService.importData(rs);
            model.addAttribute("responseRedshift", bulkImport(mainBulkObj));
        } catch(Exception ex){
            //For convenience, handle all errors here.
            ex.printStackTrace();
        }finally{
            //Finally block to close resources.
            try{
                if(stmt!=null)
                    stmt.close();
            }catch(Exception ex){
            }// nothing we can do
            try{
                if(conn!=null)
                    conn.close();
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
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
            this.logService.addLog(importStatus, requestBody);
        } catch (RestClientException e) {
            System.out.println(e.getStackTrace());
            importStatus = "Data import not successful";
            this.logService.addLog(importStatus, requestBody);
            response = e.getMessage();
        }
        return response;
    }

}
