package com.javalive.backend.dto.admin;

import java.util.List;

public record AdminImportResult(int imported, int skipped, List<String> errors) {
}
