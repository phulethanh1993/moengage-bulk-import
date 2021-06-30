package com.cmc.service;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ApiServices {
    List<JSONObject> convertToUserAttributesBulk(List<JSONObject> userDataList) {
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

    List<JSONObject> convertToDeviceAttributesBulk(List<JSONObject> userDataList) {
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

    List<JSONObject> covertToActionsBulk(List<JSONObject> actionList) {
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

    List<JSONObject> convertToLPDataBulk(List<JSONObject> LPDataList) {
        List<JSONObject> LPJsonList = new ArrayList<>();
        for (JSONObject LPData : LPDataList) {
            JSONObject LPJson = new JSONObject();
            LPJson.put("type", "customer");
            // Using "Customer ID number" as customer ID
            LPJson.put("customer_id", LPData.toMap().get("customer_id_number"));
            LPJson.put("attributes", LPData);
            LPJsonList.add(LPJson);
        }
        return LPJsonList;
    }
}
