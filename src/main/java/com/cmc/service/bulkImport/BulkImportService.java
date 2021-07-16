package com.cmc.service.bulkImport;

import com.cmc.service.importLog.MoengageImportLogService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class BulkImportService {

    @Value("${bulk.api.url}")
    private String url;

    @Value("${app.id}")
    private String userName;

    @Value("${secret.key}")
    private String password;

    private final MoengageImportLogService moengageImportLogService;

    @Autowired
    public BulkImportService(MoengageImportLogService moengageImportLogService) {
        this.moengageImportLogService = moengageImportLogService;
    }

    public String bulkImport(JSONObject requestBody, String importType) throws JsonProcessingException, HttpClientErrorException {
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
