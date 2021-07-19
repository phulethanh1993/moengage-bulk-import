package com.cmc.service.importLog;

import com.cmc.dao.MoengageImportLogDAO;
import com.cmc.model.ImportedUser;
import com.cmc.model.MoengageImportLog;
import com.cmc.service.bulkImport.ApiService;
import com.cmc.service.dbSequence.SequenceGeneratorService;
import com.cmc.utils.CustomerUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class MoengageImportLogService {

    private final MoengageImportLogDAO moengageImportLogDAO;
    private final ApiService apiService;
    private final SequenceGeneratorService sequenceGeneratorService;
    private final CustomerUtils customerUtils;

    @Autowired
    public MoengageImportLogService(MoengageImportLogDAO moengageImportLogDAO, ApiService apiService, SequenceGeneratorService sequenceGeneratorService, CustomerUtils customerUtils) {
        this.moengageImportLogDAO = moengageImportLogDAO;
        this.apiService = apiService;
        this.sequenceGeneratorService = sequenceGeneratorService;
        this.customerUtils = customerUtils;
    }

    private List<ImportedUser> getImportedUsersInfo(JSONArray usersImported) {
        List<ImportedUser> users = new ArrayList<>();
        ImportedUser importedUser = new ImportedUser();
        for (int i = 0; i < usersImported.length(); i++) {
            JSONObject user = usersImported.getJSONObject(i);
            importedUser.setCustomerId(user.get("customer_id").toString());
            importedUser.setUpdatedTime(user.getJSONObject("attributes").getLong("data_date"));
            users.add(importedUser);
        }
        return users;
    }

    public void addLog(String status, JSONObject dataImport) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        String currentDate = ZonedDateTime.now().format(formatter);

        JSONArray usersImported = dataImport.getJSONArray("elements");

        List<ImportedUser> importedUsersInfo = getImportedUsersInfo(usersImported);
        MoengageImportLog lastImported = findLastLog();
        long latestDataDate = customerUtils.findLatestDataDate(importedUsersInfo) == 0 ? lastImported.getDataDate() : customerUtils.findLatestDataDate(importedUsersInfo);
        MoengageImportLog moengageImportLog = new MoengageImportLog(currentDate, latestDataDate, status, importedUsersInfo);
        moengageImportLog.setId(sequenceGeneratorService.getSequenceNumber(MoengageImportLog.SEQUENCE_NAME));
        this.moengageImportLogDAO.insert(moengageImportLog);
    }

    public MoengageImportLog findLastLog() {
        List<MoengageImportLog> moengageImportLog = moengageImportLogDAO.findAll(Sort.by(Sort.Direction.ASC, "_id"));
        if (moengageImportLog == null || moengageImportLog.size() == 0) {
            return new MoengageImportLog();
        }
        return moengageImportLog.get(moengageImportLog.size() - 1);
    }
}
