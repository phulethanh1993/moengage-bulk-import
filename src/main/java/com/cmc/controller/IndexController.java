package com.cmc.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;
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
    private static final String[] SHEET_NAME_LIST = { "User", "Action", "Device" };

    @Value("${bulk.api.url}")
    private String url;

    @Value("${app.id}")
    private String userName;

    @Value("${secret.key}")
    private String password;

    @GetMapping("/")
    public String init() {
        return "index";
    }

    @PostMapping("/import")
    public String importData(@RequestParam("file") MultipartFile excelFile, Model model) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook(excelFile.getInputStream());

        JSONObject mainBulkObj = this.createMainBulkObject(workbook);

        model.addAttribute("response", this.bulkImport(mainBulkObj));
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
        String response = null;
        try {
            response = restTemplate.postForObject(this.url, entity, String.class);
        } catch (RestClientException e) {
            System.out.println(e.getStackTrace());
            response = e.getMessage();
        }
        return response;
    }

    private JSONObject createMainBulkObject(XSSFWorkbook workbook) {
        JSONObject mainBulkObj = new JSONObject();
        mainBulkObj.put("type", "transition");
        List<JSONObject> bulkAttribute = new ArrayList<>();
        for (String sheetName : SHEET_NAME_LIST) {
            XSSFSheet worksheet = workbook.getSheet(sheetName);
            List<JSONObject> listJsonObject = this.readValueToJsonObject(worksheet);
            switch (sheetName) {
            case "User":
                bulkAttribute.addAll(this.convertToUserAttributesBulk(listJsonObject));
                break;
            case "Device":
                bulkAttribute.addAll(this.convertToDeviceAttributesBulk(listJsonObject));
                break;
            case "Action":
                bulkAttribute.addAll(this.covertToActionsBulk(listJsonObject));
                break;
            default:
                break;
            }
        }

        mainBulkObj.put("elements", new JSONArray(bulkAttribute));
        return mainBulkObj;
    }

    private List<JSONObject> readValueToJsonObject(XSSFSheet worksheet) {
        List<JSONObject> listJSONObject = new ArrayList<>();
        Row headerRow = worksheet.getRow(0);
        // Row
        for (int i = 1; i < worksheet.getPhysicalNumberOfRows(); i++) {
            JSONObject objectJsonUser = new JSONObject();
            Row dataRow = worksheet.getRow(i);
            // Cell
            for (int j = 0; j < dataRow.getPhysicalNumberOfCells(); j++) {
                // Numeric cell
                if (dataRow.getCell(j).getCellType() == Cell.CELL_TYPE_NUMERIC) {
                    objectJsonUser.put(headerRow.getCell(j).getStringCellValue(),
                            dataRow.getCell(j).getNumericCellValue());
                    // Boolean Cell
                } else if (dataRow.getCell(j).getCellType() == Cell.CELL_TYPE_BOOLEAN) {
                    objectJsonUser.put(headerRow.getCell(j).getStringCellValue(),
                            dataRow.getCell(j).getBooleanCellValue());
                    // String Cell
                } else {
                    String cellDataValue = dataRow.getCell(j).getStringCellValue();
                    // Array Object Json
                    if (cellDataValue.contains("[{")) {
                        JSONArray jsonArray = new JSONArray(cellDataValue);
                        objectJsonUser.put(headerRow.getCell(j).getStringCellValue(), jsonArray);
                    } else if (cellDataValue.contains("{")) {
                        JSONObject jsonObject = new JSONObject(cellDataValue);
                        objectJsonUser.put(headerRow.getCell(j).getStringCellValue(), jsonObject);
                    } else {
                        objectJsonUser.put(headerRow.getCell(j).getStringCellValue(), cellDataValue);
                    }
                }
            }
            listJSONObject.add(objectJsonUser);
        }
        return listJSONObject;
    }

    private List<JSONObject> convertToUserAttributesBulk(List<JSONObject> userDataList) {
        List<JSONObject> userJsonList = new ArrayList<JSONObject>();
        for (JSONObject userData : userDataList) {
            JSONObject userJson = new JSONObject();
            userJson.put("type", "customer");
            // Using email as customer ID
            userJson.put("customer_id", userData.toMap().get("email"));
            userJson.put("attributes", userData);
            userJsonList.add(userJson);
        }
        return userJsonList;
    }

    private List<JSONObject> convertToDeviceAttributesBulk(List<JSONObject> userDataList) {
        List<JSONObject> userJsonList = new ArrayList<JSONObject>();
        for (JSONObject userData : userDataList) {
            JSONObject userJson = new JSONObject();
            userJson.put("type", "device");
            // Using email as customer ID
            userJson.put("customer_id", userData.toMap().get("email"));
            userJson.put("attributes", userData);
            userJsonList.add(userJson);
        }
        return userJsonList;
    }

    private List<JSONObject> covertToActionsBulk(List<JSONObject> actionList) {
        Map<Object, List<JSONObject>> collectByEmail = actionList.stream()
                .collect(Collectors.groupingBy(x -> x.toMap().get("email")));
        return collectByEmail.entrySet().stream().map(entry -> {
            JSONObject actionJson = new JSONObject();
            actionJson.put("type", "event");
            // Using email as customer ID
            actionJson.put("customer_id", (String) entry.getKey());
            actionJson.put("actions", new JSONArray(entry.getValue()));
            return actionJson;
        }).collect(Collectors.toList());
    }
}
