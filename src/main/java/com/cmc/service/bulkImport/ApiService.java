package com.cmc.service.bulkImport;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ApiService {

    private static final Map<String, String> dataFields = Stream.of(new String[][] {
            { "data date", "data_date" },
            { "customer id number", "customer_id_number"},
            { "cust_name", "name" },
            { "cust_first_name", "first_name" },
            { "cust_last_name", "last_name" },
            { "cust_email_id", "email" },
            { "cust_mob_no", "mobile" },
            { "cust_gender", "gender" },
            { "cust_birth_date", "age" }
    }).collect(Collectors.toConcurrentMap(data -> data[0], data -> data[1]));

    public JSONObject modifyAttributes(JSONObject dataObject) {
        dataObject.toMap().entrySet()
                .stream()
                .filter(entry -> dataFields.containsKey(entry.getKey().toLowerCase()))
                .forEach(entry -> {
                    String key = entry.getKey();
                    dataObject.put(dataFields.get(key.toLowerCase()).toLowerCase(), dataObject.get(key));
                    dataObject.remove(key);
                });
        return dataObject;
    }

    public List<JSONObject> convertToUserAttributesBulk(List<JSONObject> userDataList) {
        List<JSONObject> userJsonList = new ArrayList<>();
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

    public List<JSONObject> convertToDeviceAttributesBulk(List<JSONObject> userDataList) {
        List<JSONObject> userJsonList = new ArrayList<>();
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

    public List<JSONObject> covertToActionsBulk(List<JSONObject> actionList) {
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

    public List<JSONObject> convertToLPDataBulk(List<JSONObject> LPDataList) {
        List<JSONObject> LPJsonList = new ArrayList<>();
        for (JSONObject LPData : LPDataList) {
            JSONObject LPJson = new JSONObject();
            LPData = modifyAttributes(LPData);
            LPJson.put("type", "customer");
            // Using "Customer ID number" as customer ID
            String customerId = LPData.toMap().get("customer_id_number").toString();
            LPJson.put("customer_id", customerId);
            LPJson.put("attributes", LPData);
            LPJsonList.add(LPJson);
        }
        return LPJsonList;
    }

}
