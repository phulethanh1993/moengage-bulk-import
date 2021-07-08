package com.cmc.service;

import com.cmc.dao.MoengageImportLogDAO;
import com.cmc.model.MoengageImportLog;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class MoengageImportLogService {

    private final MoengageImportLogDAO moengageImportLogDAO;
    private final ApiService apiService;
    private SequenceGeneratorService sequenceGeneratorService;

    @Autowired
    public MoengageImportLogService(MoengageImportLogDAO moengageImportLogDAO, ApiService apiService, SequenceGeneratorService sequenceGeneratorService) {
        this.moengageImportLogDAO = moengageImportLogDAO;
        this.apiService = apiService;
        this.sequenceGeneratorService = sequenceGeneratorService;
    }

    private List<JSONObject> getImportedUsersInfo(JSONArray usersImported) {
        List<JSONObject> users = new ArrayList<>();
        for (int i = 0; i < usersImported.length(); i++) {
            users.add(usersImported.getJSONObject(i));
        }
        ArrayList<JSONObject> importantProps = new ArrayList<>();
        for (JSONObject user : users) {
            JSONObject prop = new JSONObject();
            prop.put("customer_id", user.get("customer_id"));
            prop.put("updated_time", user.getJSONObject("attributes").get("data_date"));
            importantProps.add(prop);
        }
        return importantProps;
    }

    public void addLog(String status, JSONObject dataImport) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        String currentDate = ZonedDateTime.now().format(formatter);
        JSONArray usersImported = dataImport.getJSONArray("elements");

        List<JSONObject> importedUsersInfo = getImportedUsersInfo(usersImported);
        MoengageImportLog moengageImportLog = new MoengageImportLog(currentDate, status, dataImport, importedUsersInfo);
        moengageImportLog.setId(sequenceGeneratorService.getSequenceNumber(moengageImportLog.SEQUENCE_NAME));
        this.moengageImportLogDAO.insert(moengageImportLog);
    }

    public MoengageImportLog findLastLog() {
        return this.moengageImportLogDAO.findFirstByOrderByIdDesc();
    }
}
