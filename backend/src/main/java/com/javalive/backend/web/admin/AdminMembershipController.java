package com.javalive.backend.web.admin;

import com.javalive.backend.service.external.OnlineTraderApiClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Proxies the external app.getonlinetrader.pro courses/lessons/categories API, mirroring
 * {@code Admin\MembershipController} exactly. Per the standing decision, built correctly and
 * wired but not exercised against a live upstream — the migrated {@code merchant_key} is null,
 * matching source's own never-configured state.
 */
@RestController
@RequestMapping("/api/admin/membership")
public class AdminMembershipController {

    private final OnlineTraderApiClient apiClient;

    public AdminMembershipController(OnlineTraderApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @GetMapping("/courses")
    public Map<String, Object> courses(@RequestParam(required = false) String searchValue) {
        return apiClient.get("/courses", Map.of("value", searchValue == null ? "" : searchValue));
    }

    @PostMapping("/courses")
    public Map<String, Object> addCourse(@RequestBody Map<String, Object> body) {
        return apiClient.post("/add-course", body);
    }

    @PutMapping("/courses")
    public Map<String, Object> updateCourse(@RequestBody Map<String, Object> body) {
        return apiClient.post("/update-course", body);
    }

    @DeleteMapping("/courses/{id}")
    public Map<String, Object> deleteCourse(@PathVariable String id) {
        return apiClient.delete("/delete-course/" + id);
    }

    @GetMapping("/courses/{id}/lessons")
    public Map<String, Object> lessons(@PathVariable String id) {
        return apiClient.get("/courses-lessons/" + id, Map.of());
    }

    @PostMapping("/lessons")
    public Map<String, Object> addLesson(@RequestBody Map<String, Object> body) {
        return apiClient.post("/add-lesson", body);
    }

    @PutMapping("/lessons")
    public Map<String, Object> updateLesson(@RequestBody Map<String, Object> body) {
        return apiClient.post("/update-lesson", body);
    }

    @DeleteMapping("/lessons/{id}")
    public Map<String, Object> deleteLesson(@PathVariable String id) {
        return apiClient.delete("/delete-lesson/" + id);
    }

    @GetMapping("/lessons-without-course")
    public Map<String, Object> lessonsWithoutCourse() {
        return apiClient.get("/lessons-without-course", Map.of());
    }

    @GetMapping("/categories")
    public Map<String, Object> categories() {
        return apiClient.get("/categories", Map.of());
    }

    @PostMapping("/categories")
    public Map<String, Object> addCategory(@RequestBody Map<String, Object> body) {
        return apiClient.post("/add-category", body);
    }

    @DeleteMapping("/categories/{id}")
    public Map<String, Object> deleteCategory(@PathVariable String id) {
        return apiClient.delete("/delete-cat/" + id);
    }
}
