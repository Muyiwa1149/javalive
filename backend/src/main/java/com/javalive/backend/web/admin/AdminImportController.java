package com.javalive.backend.web.admin;

import com.javalive.backend.dto.admin.AdminImportResult;
import com.javalive.backend.service.admin.AdminImportService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/import")
public class AdminImportController {

    private final AdminImportService adminImportService;

    public AdminImportController(AdminImportService adminImportService) {
        this.adminImportService = adminImportService;
    }

    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() {
        byte[] file = adminImportService.downloadTemplate();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("leads.xlsx").build().toString())
                .body(file);
    }

    @PostMapping(consumes = "multipart/form-data")
    public AdminImportResult importUsers(@RequestParam("file") MultipartFile file) {
        return adminImportService.importUsers(file);
    }
}
