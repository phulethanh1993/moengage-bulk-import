package com.cmc.service.inputData;

import com.cmc.dto.ResourceDTO;
import com.cmc.model.DbInfo;
import com.cmc.model.ImportedUser;
import com.cmc.model.MoengageImportLog;
import com.cmc.service.importLog.MoengageImportLogService;
import com.cmc.service.resource.ResourceService;
import com.cmc.utils.CustomerUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;

@Service
public class RedshiftClusterImportService implements ResourceService {

    @Value("${secret.dbURL}")
    private String dbURL;

    @Value("${secret.masterUsername}")
    private String MasterUsername;

    @Value("${secret.masterUserPassword}")
    private String MasterUserPassword;

    private MoengageImportLogService moengageImportLogService;
    private CustomerUtils customerUtils;

    @Autowired
    public RedshiftClusterImportService(MoengageImportLogService moengageImportLogService, CustomerUtils customerUtils) {
        this.moengageImportLogService = moengageImportLogService;
        this.customerUtils = customerUtils;
    }

    public ResultSet initConnection(String schema, String table) throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", MasterUsername);
        props.setProperty("password", MasterUserPassword);
        Connection conn = DriverManager.getConnection(dbURL, props);
        long latestDataDate = getLatestDataDate();
        ResultSet rs;
        String sql = "select * from %s.%s where data_date > ?";
        sql = String.format(sql, schema, table);
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setLong(1, latestDataDate);
        rs = ps.executeQuery();
        return rs;
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

    @Override
    public ResourceDTO getResources(Object object) {
        DbInfo dbInfo = (DbInfo) object;
        ResourceDTO resourceDTO = new ResourceDTO();
        Map<String, List<JSONObject>> tablesFetching = new HashMap<>();
        ResultSet rs;
        List<JSONObject> dataList;
        try {
            rs = initConnection(dbInfo.getSchema(), dbInfo.getTableName());
            dataList = readTableToJSONObject(rs);
            tablesFetching.put(dbInfo.getTableName(), dataList);
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        resourceDTO.setDataImport(tablesFetching);
        return resourceDTO;
    }

    @Override
    public long getLatestDataDate() {
        MoengageImportLog lastImported = moengageImportLogService.findLastLog();
        List<ImportedUser> lastImportedUsers = lastImported.getImportedUsers();
        long latestDataDate = lastImported.getDataDate() == 0 ? customerUtils.findLatestDataDate(lastImportedUsers) : lastImported.getDataDate();
        return latestDataDate;
    }
}