package com.cmc.controller;

import com.cmc.dto.ResourceDTO;
import com.cmc.model.DbInfo;
import com.cmc.service.bulkImport.BulkImportService;
import com.cmc.service.inputData.CSVFileImportService;
import com.cmc.service.inputData.ExcelFileImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
public class IndexController {

    private String IMPORT_TYPE = "";
    private final ExcelFileImportService excelFileImportService;
    private final CSVFileImportService csvFileImportService;
    private final BulkImportService bulkImportService;
    private final JobController jobController;

    @Autowired
    public IndexController(ExcelFileImportService excelFileImportService, CSVFileImportService csvFileImportService, BulkImportService bulkImportService, JobController jobController) {
        this.excelFileImportService = excelFileImportService;
        this.csvFileImportService = csvFileImportService;
        this.bulkImportService = bulkImportService;
        this.jobController = jobController;
    }

    @GetMapping("/")
    public String init() {
        return "index";
    }

    @PostMapping("/import-excel")
    public String importExcel(@RequestParam("excel") MultipartFile excelFile, Model model) throws IOException {
        IMPORT_TYPE = "Excel";
        ResourceDTO resourceDTO = excelFileImportService.getResources(excelFile);
        String resp = bulkImportService.bulkImport(resourceDTO, IMPORT_TYPE);
        model.addAttribute("responseExcel", resp);
        return "index";
    }

    @PostMapping("/import-csv")
    public String importCSV(@RequestParam("csv") MultipartFile csvFile, Model model) throws IOException {
        IMPORT_TYPE = "Excel";
        ResourceDTO resourceDTO = csvFileImportService.getResources(csvFile);
        String resp = bulkImportService.bulkImport(resourceDTO, IMPORT_TYPE);
        model.addAttribute("responseCSV", resp);
        return "index";
    }

    @PostMapping("/import-redshift")
    @ResponseBody
    public String importDataFromRedshift(Model model) {
        IMPORT_TYPE = "Redshift";
        DbInfo dbInfo = new DbInfo("dev", "public", "sbf_loan_portfolio");
        jobController.runBulkImportJob(dbInfo);
        return "";
    }

}