package com.cmc.service.bulkImport;

import com.cmc.dto.ResourceDTO;
import com.cmc.service.importLog.MoengageImportLogService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class BulkImportService extends ApiService {

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

    private List<JSONObject> populateBulkAttributes(ResourceDTO resourceDTO) {
        Map<String, List<JSONObject>> dataImport = resourceDTO.getDataImport();
        List<JSONObject> bulkAttribute = new ArrayList<>();
        boolean readingDone = false;
        for (Map.Entry<String, List<JSONObject>> entry : dataImport.entrySet()) {
            if (readingDone) {
                break;
            }
            switch (entry.getKey()) {
                case "sbf_loan_portfolio":
                case "LP Data Sample":
                    bulkAttribute.addAll(this.convertToLPDataBulk(entry.getValue()));
                    readingDone = true;
                    break;
                case "User":
                    bulkAttribute.addAll(this.convertToUserAttributesBulk(entry.getValue()));
                    break;
                case "Device":
                    bulkAttribute.addAll(this.convertToDeviceAttributesBulk(entry.getValue()));
                    break;
                case "Action":
                    bulkAttribute.addAll(this.covertToActionsBulk(entry.getValue()));
                    break;
                default:
                    break;
            }
        }
        return bulkAttribute;
    }

    public JSONObject createMainBulkObject(ResourceDTO resourceDTO) {
        JSONObject mainBulkObj = new JSONObject();
        mainBulkObj.put("type", "transition");
        List<JSONObject> bulkAttribute = populateBulkAttributes(resourceDTO);
        mainBulkObj.put("elements", new JSONArray(bulkAttribute));
        return mainBulkObj;
    }

    public String bulkImport(ResourceDTO resourceDTO, String importType) throws JsonProcessingException, HttpClientErrorException {
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper mapper = new ObjectMapper();
        JSONObject mainBulkObject = createMainBulkObject(resourceDTO);
        String requestJson = mapper.writeValueAsString(mainBulkObject.toMap());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(this.userName, this.password);

        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);
        String response = null;
        String importStatus;
        try {
            response = restTemplate.postForObject(this.url, entity, String.class);
            importStatus = "Successfully imported data from " + importType;
            this.moengageImportLogService.addLog(importStatus, mainBulkObject);
        } catch (HttpClientErrorException e) {
            System.out.println(e.getStackTrace());
            if (mainBulkObject.getJSONArray("elements").length() == 0) {
                importStatus = "Error. Data import cannot be empty.";
            } else {
                importStatus = "Data import not successful, please check the database connectivity.";
            }
            this.moengageImportLogService.addLog(importStatus, mainBulkObject);
            HttpStatus status = e.getStatusCode();
            if (status == HttpStatus.BAD_REQUEST) {
                response = "Bad request. Data to import is either empty or the database connectivity is incorrect. Detail: " + e.getResponseBodyAsString();;
            }
        }
        return response;
    }
}
