package com.cmc.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class ConnectToClusterService extends ApiServices {

    public JSONObject importData(ResultSet rs) throws IOException, SQLException {
        List<JSONObject> dataList = readTableToJSONObject(rs);
        JSONObject mainBulkObj = createMainBulkObject(dataList);
        return mainBulkObj;
    }

    private List<JSONObject> readTableToJSONObject(ResultSet rs) throws SQLException {
        List<JSONObject> listJSONObject = new ArrayList<>();
        while (rs.next()) {
            JSONObject obj = new JSONObject();
            int total_cols = rs.getMetaData().getColumnCount();
            for (int i = 0; i < total_cols; i++) {
                obj.put(rs.getMetaData().getColumnLabel(i + 1)
                        .toLowerCase(), rs.getObject(i + 1));

            }
            listJSONObject.add(obj);
        }
        return listJSONObject;
    }

    public JSONObject createMainBulkObject(List<JSONObject> listJsonObject) {
        JSONObject mainBulkObj = new JSONObject();
        mainBulkObj.put("type", "transition");
        List<JSONObject> bulkAttribute = new ArrayList<>();
        bulkAttribute.addAll(this.convertToLPDataBulk(listJsonObject));
        mainBulkObj.put("elements", new JSONArray(bulkAttribute));
        return mainBulkObj;
    }

}