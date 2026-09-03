package com.javalive.backend.service.admin;

import com.javalive.backend.dto.admin.AdminImportResult;
import com.javalive.backend.entity.User;
import com.javalive.backend.repository.UserRepository;
import com.javalive.backend.web.exception.ApiException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Mirrors the source app's {@code Admin\ImportController} + {@code App\Imports\UsersImport} (Maatwebsite/Excel) exactly. */
@Service
public class AdminImportService {

    private static final String DEFAULT_PASSWORD = "password";
    private static final List<String> HEADERS = List.of("name", "email", "username", "country", "phone_number");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminUserService adminUserService;

    public AdminImportService(UserRepository userRepository, PasswordEncoder passwordEncoder, AdminUserService adminUserService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminUserService = adminUserService;
    }

    public byte[] downloadTemplate() {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("leads");
            Row header = sheet.createRow(0);
            for (int i = 0; i < HEADERS.size(); i++) {
                header.createCell(i).setCellValue(HEADERS.get(i));
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate template.");
        }
    }

    @Transactional
    public AdminImportResult importUsers(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Please choose a file to import.");
        }

        int imported = 0;
        List<String> errors = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "The file has no header row.");
            }
            Map<String, Integer> columnIndex = new HashMap<>();
            for (Cell cell : headerRow) {
                columnIndex.put(cellText(cell).trim().toLowerCase(), cell.getColumnIndex());
            }

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                String name = valueOf(row, columnIndex, "name");
                String email = valueOf(row, columnIndex, "email");
                String username = valueOf(row, columnIndex, "username");
                String country = valueOf(row, columnIndex, "country");
                String phone = valueOf(row, columnIndex, "phone_number");

                if (name.isBlank() && email.isBlank()) continue;

                if (email.isBlank() || username.isBlank()) {
                    errors.add("Row " + (r + 1) + ": missing required email or username.");
                    continue;
                }
                if (userRepository.existsByEmail(email)) {
                    errors.add("Row " + (r + 1) + ": email " + email + " already exists.");
                    continue;
                }
                if (userRepository.existsByUsername(username)) {
                    errors.add("Row " + (r + 1) + ": username " + username + " already exists.");
                    continue;
                }

                User user = adminUserService.newUserDefaults(name, username, email,
                        passwordEncoder.encode(DEFAULT_PASSWORD), country, phone);
                user.setEmailVerifiedAt(LocalDateTime.now());
                userRepository.save(user);
                imported++;
            }
        } catch (IOException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Could not read the uploaded file — please upload a valid .xlsx or .xls file.");
        }

        return new AdminImportResult(imported, errors.size(), errors);
    }

    private String valueOf(Row row, Map<String, Integer> columnIndex, String header) {
        Integer idx = columnIndex.get(header);
        if (idx == null) return "";
        Cell cell = row.getCell(idx);
        return cell == null ? "" : cellText(cell).trim();
    }

    private String cellText(Cell cell) {
        if (cell.getCellType() == CellType.NUMERIC) {
            double value = cell.getNumericCellValue();
            return value == Math.floor(value) ? String.valueOf((long) value) : String.valueOf(value);
        }
        return cell.toString();
    }
}
