package com.cmc.service.inputData;

import com.cmc.dto.ResourceDTO;
import com.cmc.service.resource.ResourceService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.commons.csv.CSVParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CSVFileImportService implements ResourceService {

    public ResourceDTO setResourceDTO(CSVParser csvParser, String fileName) throws IOException {
        ResourceDTO resourceDTO = new ResourceDTO();
        List<JSONObject> records = readValueToJsonObject(csvParser);
        Map<String, List<JSONObject>> resource = new HashMap<>();
        resource.put(fileName, records);
        resourceDTO.setDataImport(resource);
        return resourceDTO;
    }

    private List<JSONObject> readValueToJsonObject(CSVParser csvParser) throws IOException {
        List<JSONObject> csvData = new ArrayList<>();
        List<CSVRecord> csvRecords = csvParser.getRecords();
        CSVRecord header = csvRecords.get(0);
        int rows = csvRecords.size();
        int cells = header.size();
        // Row
        for (int i = 1; i < rows; i++) {
            JSONObject objectJsonUser = new JSONObject();
            CSVRecord row = csvRecords.get(i);
            // Cell
            for (int j = 0; j < cells; j++) {
                objectJsonUser.put(header.get(j), row.get(j));
            }
            csvData.add(objectJsonUser);
        }
        return csvData;
    }

    @Override
    public ResourceDTO getResources(Object data) {
        MultipartFile csvFile = (MultipartFile) data;
        String fileName = csvFile.getOriginalFilename();
        CSVParser csvParser;
        ResourceDTO resourceDTO = null;
        try {
            BufferedReader fileReader = new BufferedReader(new
                    InputStreamReader(csvFile.getInputStream(), "UTF-8"));
            csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT);
            resourceDTO = setResourceDTO(csvParser, fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resourceDTO;
    }

    @Override
    public long getLatestDataDate() {
        return 0;
    }
}
