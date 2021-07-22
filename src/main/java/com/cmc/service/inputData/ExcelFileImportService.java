package com.cmc.service.inputData;

import com.cmc.dto.ResourceDTO;
import com.cmc.model.ImportedUser;
import com.cmc.model.MoengageImportLog;
import com.cmc.service.bulkImport.ApiService;
import com.cmc.service.importLog.MoengageImportLogService;
import com.cmc.service.resource.ResourceService;
import com.cmc.utils.CustomerUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExcelFileImportService implements ResourceService {

    private MoengageImportLogService moengageImportLogService;
    private CustomerUtils customerUtils;

    @Autowired
    public ExcelFileImportService(MoengageImportLogService moengageImportLogService, CustomerUtils customerUtils) {
        this.moengageImportLogService = moengageImportLogService;
        this.customerUtils = customerUtils;
    }

    private List<String> getSheetNames(XSSFWorkbook workbook) {
        List<String> sheetNames = new ArrayList<>();
        for (int i=0; i<workbook.getNumberOfSheets(); i++) {
            sheetNames.add(workbook.getSheetName(i));
        }
        return sheetNames;
    }

    public ResourceDTO setResourceDTO(XSSFWorkbook workbook, List<String> sheetNames) {
        ResourceDTO resourceDTO = new ResourceDTO();
        Map<String, List<JSONObject>> sheetsInFile = new HashMap<>();
        long latestDataDate = getLatestDataDate();
        for (String sheetName : sheetNames) {
            if (sheetName.equals("LP Data Sample")) {
                XSSFSheet worksheet = workbook.getSheet(sheetName);
                List<JSONObject> listJsonObject = this.readValueToJsonObject(worksheet, latestDataDate);
                sheetsInFile.put(sheetName, listJsonObject);
            }
        }
        resourceDTO.setDataImport(sheetsInFile);
        return resourceDTO;
    }

    private List<JSONObject> readValueToJsonObject(XSSFSheet worksheet, long latestDataDate) {
        List<JSONObject> listJSONObject = new ArrayList<>();
        Row headerRow = worksheet.getRow(0);
        int rows = worksheet.getPhysicalNumberOfRows();
        // Row
        for (int i = 1; i < rows; i++) {
            JSONObject objectJsonUser = new JSONObject();
            Row dataRow = worksheet.getRow(i);
            boolean skippedRow = false;
            int cells = dataRow.getPhysicalNumberOfCells();
            // Cell
            for (int j = 0; j < cells; j++) {
                if (headerRow.getCell(j).getStringCellValue().equals("Data Date") && dataRow.getCell(j).getNumericCellValue() <= latestDataDate) {
                    skippedRow = true;
                    break;
                }
                // Numeric cell
                if (dataRow.getCell(j).getCellType() == Cell.CELL_TYPE_NUMERIC) {
                    objectJsonUser.put(headerRow.getCell(j).getStringCellValue(),
                            dataRow.getCell(j).getNumericCellValue());
                    // Boolean Cell
                } else if (dataRow.getCell(j).getCellType() == Cell.CELL_TYPE_BOOLEAN) {
                    objectJsonUser.put(headerRow.getCell(j).getStringCellValue(),
                            dataRow.getCell(j).getBooleanCellValue());
                    // Formula Cell
                } else if (dataRow.getCell(j).getCellType() == Cell.CELL_TYPE_FORMULA) {
                    if (dataRow.getCell(j).getCachedFormulaResultType() == Cell.CELL_TYPE_NUMERIC) {
                        objectJsonUser.put(headerRow.getCell(j).getStringCellValue(),
                                dataRow.getCell(j).getNumericCellValue());
                    }
                    // String Cell
                }
                else {
                    String cellDataValue = dataRow.getCell(j).getStringCellValue();
                    // Array Object Json
                    if (cellDataValue.contains("[{")) {
                        JSONArray jsonArray = new JSONArray(cellDataValue);
                        objectJsonUser.put(headerRow.getCell(j).getStringCellValue(), jsonArray);
                    } else if (cellDataValue.contains("{")) {
                        JSONObject jsonObject = new JSONObject(cellDataValue);
                        objectJsonUser.put(headerRow.getCell(j).getStringCellValue(), jsonObject);
                    } else {
                        objectJsonUser.put(headerRow.getCell(j).getStringCellValue(), cellDataValue);
                    }
                }
            }
            if (skippedRow) {
                continue;
            }
            listJSONObject.add(objectJsonUser);
        }
        return listJSONObject;
    }

    @Override
    public ResourceDTO getResources(Object object) {
        MultipartFile excelFile = (MultipartFile) object;
        XSSFWorkbook workbook = null;
        try {
            workbook = new XSSFWorkbook(excelFile.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<String> sheetNames = getSheetNames(workbook);
        ResourceDTO resourceDTO = setResourceDTO(workbook, sheetNames);
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