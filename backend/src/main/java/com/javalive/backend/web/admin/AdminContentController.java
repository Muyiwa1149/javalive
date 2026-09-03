package com.javalive.backend.web.admin;

import com.javalive.backend.dto.admin.AdminCmsImageSummary;
import com.javalive.backend.dto.admin.AdminContentRequest;
import com.javalive.backend.dto.admin.AdminContentSummary;
import com.javalive.backend.dto.admin.AdminFaqRequest;
import com.javalive.backend.dto.admin.AdminFaqSummary;
import com.javalive.backend.dto.admin.AdminTermsPrivacyRequest;
import com.javalive.backend.dto.admin.AdminTermsPrivacySummary;
import com.javalive.backend.dto.admin.AdminTestimonyRequest;
import com.javalive.backend.dto.admin.AdminTestimonySummary;
import com.javalive.backend.service.admin.AdminContentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin/content")
public class AdminContentController {

    private final AdminContentService adminContentService;

    public AdminContentController(AdminContentService adminContentService) {
        this.adminContentService = adminContentService;
    }

    @GetMapping("/faqs")
    public List<AdminFaqSummary> listFaqs() {
        return adminContentService.listFaqs();
    }

    @PostMapping("/faqs")
    public AdminFaqSummary createFaq(@Valid @RequestBody AdminFaqRequest request) {
        return adminContentService.createFaq(request);
    }

    @PutMapping("/faqs/{id}")
    public AdminFaqSummary updateFaq(@PathVariable Long id, @Valid @RequestBody AdminFaqRequest request) {
        return adminContentService.updateFaq(id, request);
    }

    @DeleteMapping("/faqs/{id}")
    public void deleteFaq(@PathVariable Long id) {
        adminContentService.deleteFaq(id);
    }

    @GetMapping("/testimonials")
    public List<AdminTestimonySummary> listTestimonials() {
        return adminContentService.listTestimonials();
    }

    @PostMapping("/testimonials")
    public AdminTestimonySummary createTestimonial(@Valid @RequestBody AdminTestimonyRequest request) {
        return adminContentService.createTestimonial(request);
    }

    @PutMapping("/testimonials/{id}")
    public AdminTestimonySummary updateTestimonial(@PathVariable Long id, @Valid @RequestBody AdminTestimonyRequest request) {
        return adminContentService.updateTestimonial(id, request);
    }

    @DeleteMapping("/testimonials/{id}")
    public void deleteTestimonial(@PathVariable Long id) {
        adminContentService.deleteTestimonial(id);
    }

    @GetMapping("/images")
    public List<AdminCmsImageSummary> listImages() {
        return adminContentService.listImages();
    }

    @PostMapping(value = "/images", consumes = "multipart/form-data")
    public AdminCmsImageSummary createImage(@RequestParam String title, @RequestParam String description,
                                             @RequestParam MultipartFile image) {
        return adminContentService.createImage(title, description, image);
    }

    @PutMapping(value = "/images/{id}", consumes = "multipart/form-data")
    public AdminCmsImageSummary updateImage(@PathVariable Long id, @RequestParam String title,
                                             @RequestParam String description,
                                             @RequestParam(required = false) MultipartFile image) {
        return adminContentService.updateImage(id, title, description, image);
    }

    @GetMapping("/pages")
    public List<AdminContentSummary> listContents() {
        return adminContentService.listContents();
    }

    @PostMapping("/pages")
    public AdminContentSummary createContent(@Valid @RequestBody AdminContentRequest request) {
        return adminContentService.createContent(request);
    }

    @PutMapping("/pages/{id}")
    public AdminContentSummary updateContent(@PathVariable Long id, @Valid @RequestBody AdminContentRequest request) {
        return adminContentService.updateContent(id, request);
    }

    @GetMapping("/privacy-policy")
    public AdminTermsPrivacySummary getTermsPrivacy() {
        return AdminTermsPrivacySummary.from(adminContentService.getTermsPrivacy());
    }

    @PutMapping("/privacy-policy")
    public AdminTermsPrivacySummary updateTermsPrivacy(@Valid @RequestBody AdminTermsPrivacyRequest request) {
        return AdminTermsPrivacySummary.from(adminContentService.updateTermsPrivacy(request));
    }
}
