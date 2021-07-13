package com.cmc.service;

import com.cmc.model.ImportedUser;
import com.cmc.model.MoengageImportLog;
import com.cmc.utils.RedShiftUtils;
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
import java.util.List;

@Service
public class ExcelFileImportService extends ApiService {

    private MoengageImportLogService moengageImportLogService;
    private RedShiftUtils redShiftUtils;

    @Autowired
    public ExcelFileImportService(MoengageImportLogService moengageImportLogService, RedShiftUtils redShiftUtils) {
        this.moengageImportLogService = moengageImportLogService;
        this.redShiftUtils = redShiftUtils;
    }


    public JSONObject importData(MultipartFile excelFile, String apiKey) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook(excelFile.getInputStream());
        List<String> sheetNames = getSheetNames(workbook);
        JSONObject mainBulkObj = createMainBulkObject(workbook, sheetNames, apiKey);
        return mainBulkObj;
    }

    private List<String> getSheetNames(XSSFWorkbook workbook) {
        List<String> sheetNames = new ArrayList<>();
        for (int i=0; i<workbook.getNumberOfSheets(); i++) {
            sheetNames.add(workbook.getSheetName(i));
        }
        return sheetNames;
    }

    public JSONObject createMainBulkObject(XSSFWorkbook workbook, List<String> sheetNames, String apiKey) {
        JSONObject mainBulkObj = new JSONObject();
        mainBulkObj.put("type", "transition");
        List<JSONObject> bulkAttribute = new ArrayList<>();
        MoengageImportLog lastImported = moengageImportLogService.findLastLog();
        List<ImportedUser> lastImportedUsers = lastImported.getImportedUsers();
        long latestDataDate = lastImported.getDataDate() == 0 ? redShiftUtils.findLatestDataDate(lastImportedUsers) : lastImported.getDataDate();
        boolean readingDone = false;
        for (String sheetName : sheetNames) {
            if (readingDone) {
                break;
            }
            XSSFSheet worksheet = workbook.getSheet(sheetName);
            List<JSONObject> listJsonObject = this.readValueToJsonObject(worksheet, latestDataDate);
            switch (sheetName) {
                case "LP Data Sample":
                    bulkAttribute.addAll(this.convertToLPDataBulk(listJsonObject, apiKey));
                    readingDone = true;
                    break;
                case "User":
                    bulkAttribute.addAll(this.convertToUserAttributesBulk(listJsonObject));
                    break;
                case "Device":
                    bulkAttribute.addAll(this.convertToDeviceAttributesBulk(listJsonObject));
                    break;
                case "Action":
                    bulkAttribute.addAll(this.covertToActionsBulk(listJsonObject));
                    break;
                default:
                    break;
            }
        }
        mainBulkObj.put("elements", new JSONArray(bulkAttribute));
        return mainBulkObj;
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

}