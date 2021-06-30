package com.cmc.service;

import com.cmc.dao.LogDAO;
import com.cmc.model.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Service
public class LogService {

    private final LogDAO logDAO;

    public LogService(LogDAO logDAO) {
        this.logDAO = logDAO;
    }

    public void addLog(String status, JSONObject dataImport) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        String currentDate = ZonedDateTime.now().format(formatter);
        Log log = new Log(currentDate, status, dataImport);
        this.logDAO.insert(log);
    }
}
