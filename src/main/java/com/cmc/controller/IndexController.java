package com.cmc.controller;

import com.cmc.dto.ResourceDTO;
import com.cmc.model.DbInfo;
import com.cmc.service.bulkImport.BulkImportService;
import com.cmc.service.inputData.ExcelFileImportService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLException;

@Controller
public class IndexController {

    private String IMPORT_TYPE = "";
    private final ExcelFileImportService excelFileImportService;
    private final BulkImportService bulkImportService;
    private final JobController jobController;

    @Autowired
    public IndexController(ExcelFileImportService excelFileImportService, BulkImportService bulkImportService, JobController jobController) {
        this.excelFileImportService = excelFileImportService;
        this.bulkImportService = bulkImportService;
        this.jobController = jobController;
    }

    @GetMapping("/")
    public String init() {
        return "index";
    }

    @PostMapping("/import")
    public String importData(@RequestParam("file") MultipartFile excelFile, Model model) throws IOException {
        IMPORT_TYPE = "Excel";
        ResourceDTO resourceDTO = excelFileImportService.getResources(excelFile);
        String resp = bulkImportService.bulkImport(resourceDTO, IMPORT_TYPE);
        model.addAttribute("responseExcel", resp);
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