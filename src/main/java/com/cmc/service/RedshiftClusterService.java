package com.cmc.service;

import com.cmc.model.ImportedUser;
import com.cmc.model.MoengageImportLog;
import com.cmc.utils.RedShiftUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Service
public class RedshiftClusterService extends ApiService {

    @Value("${secret.dbURL}")
    private String dbURL;

    @Value("${secret.masterUsername}")
    private String MasterUsername;

    @Value("${secret.masterUserPassword}")
    private String MasterUserPassword;

    private MoengageImportLogService moengageImportLogService;
    private RedShiftUtils redShiftUtils;

    @Autowired
    public RedshiftClusterService(MoengageImportLogService moengageImportLogService, RedShiftUtils redShiftUtils) {
        this.moengageImportLogService = moengageImportLogService;
        this.redShiftUtils = redShiftUtils;
    }

    public ResultSet initConnection() throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", MasterUsername);
        props.setProperty("password", MasterUserPassword);
        Connection conn = DriverManager.getConnection(dbURL, props);
        MoengageImportLog lastImported = moengageImportLogService.findLastLog();
        List<ImportedUser> lastImportedUsers = lastImported.getImportedUsers();
        long latestDataDate = lastImported.getDataDate() == 0 ? redShiftUtils.findLatestDataDate(lastImportedUsers) : lastImported.getDataDate();
        ResultSet rs;
        String sql = "select * from public.sbf_loan_portfolio where data_date > ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setLong(1, latestDataDate);
        rs = ps.executeQuery();
        return rs;
    }

    public JSONObject importData(String apiKey) throws SQLException {
        ResultSet rs = initConnection();
        List<JSONObject> dataList = readTableToJSONObject(rs);
        JSONObject mainBulkObj = createMainBulkObject(dataList, apiKey);
        rs.close();
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

    public JSONObject createMainBulkObject(List<JSONObject> listJsonObject, String apiKey) {
        JSONObject mainBulkObj = new JSONObject();
        mainBulkObj.put("type", "transition");
        List<JSONObject> bulkAttribute = new ArrayList<>();
        bulkAttribute.addAll(this.convertToLPDataBulk(listJsonObject, apiKey));
        mainBulkObj.put("elements", new JSONArray(bulkAttribute));
        return mainBulkObj;
    }

}