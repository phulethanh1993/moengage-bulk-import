package com.cmc.service.inputData;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ApiService {

    private static final Map<String, String> dataFields = Stream.of(new String[][] {
            { "Data Date", "data_date" },
            { "Customer ID number", "customer_id_number"},
            { "cust_name", "name" },
            { "cust_first_name", "first_name" },
            { "cust_last_name", "last_name" },
            { "cust_email_id", "email" },
            { "cust_mob_no", "mobile" },
            { "cust_gender", "gender" },
            { "cust_birth_date", "age" }
    }).collect(Collectors.toConcurrentMap(data -> data[0], data -> data[1]));

    private GeoApiContext context = null;
    private final Map<String, String> countriesCode = new HashMap<>();
    private final DecimalFormat df = new DecimalFormat("0.00");

    public JSONObject modifyAttributes(JSONObject dataObject, String apiKey) {
        if (countriesCode.isEmpty()) {
            getCountryCode();
        }
        if (context == null) {
            context = new GeoApiContext.Builder().apiKey(apiKey).build();
        }
        dataObject.toMap().entrySet()
                .stream()
                .filter(entry -> dataFields.containsKey(entry.getKey()))
                .forEach(entry -> {
                    String key = entry.getKey();
                    dataObject.put(dataFields.get(key), dataObject.get(key));
                    dataObject.remove(key);
                });
        getGeolocation(dataObject);
        return dataObject;
    }

    private void getGeolocation(JSONObject dataObject) {
        String presentAddress = dataObject.getString("cust_present_address");
        String country = dataObject.getString("cust_present_country_of_residence");
        GeocodingResult[] results;
        try {
            results =  GeocodingApi.geocode(context,
                    presentAddress + ", " + countriesCode.get(country)).await();
            String addr =  "{'lat': " + df.format(results[0].geometry.location.lat) +", 'lon': " + df.format(results[0].geometry.location.lng) + "}";
            dataObject.put("moe_geo_location", addr);
        } catch (ApiException | InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    private void getCountryCode() {
        for (String iso : Locale.getISOCountries()) {
            Locale l = new Locale("", iso);
            this.countriesCode.put(l.getISO3Country(), l.getDisplayCountry());
        }
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

    public List<JSONObject> convertToLPDataBulk(List<JSONObject> LPDataList, String apiKey) {
        List<JSONObject> LPJsonList = new ArrayList<>();
        for (JSONObject LPData : LPDataList) {
            JSONObject LPJson = new JSONObject();
            LPData = modifyAttributes(LPData, apiKey);
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
