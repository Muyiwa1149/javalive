package com.javalive.backend.service.admin;

import com.javalive.backend.dto.admin.AdminCmsImageSummary;
import com.javalive.backend.dto.admin.AdminContentRequest;
import com.javalive.backend.dto.admin.AdminContentSummary;
import com.javalive.backend.dto.admin.AdminFaqRequest;
import com.javalive.backend.dto.admin.AdminFaqSummary;
import com.javalive.backend.dto.admin.AdminTermsPrivacyRequest;
import com.javalive.backend.dto.admin.AdminTestimonyRequest;
import com.javalive.backend.dto.admin.AdminTestimonySummary;
import com.javalive.backend.entity.CmsImage;
import com.javalive.backend.entity.Content;
import com.javalive.backend.entity.Faq;
import com.javalive.backend.entity.TermsPrivacy;
import com.javalive.backend.entity.Testimony;
import com.javalive.backend.repository.CmsImageRepository;
import com.javalive.backend.repository.ContentRepository;
import com.javalive.backend.repository.FaqRepository;
import com.javalive.backend.repository.TermsPrivacyRepository;
import com.javalive.backend.repository.TestimonyRepository;
import com.javalive.backend.service.storage.FileStorageService;
import com.javalive.backend.web.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

/** Mirrors the source app's {@code Admin\FrontendController} exactly — including its two real gaps: images and page content have no delete action. */
@Service
public class AdminContentService {

    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = List.of("jpg", "jpeg", "png");
    private final SecureRandom random = new SecureRandom();

    private final FaqRepository faqRepository;
    private final TestimonyRepository testimonyRepository;
    private final CmsImageRepository cmsImageRepository;
    private final ContentRepository contentRepository;
    private final TermsPrivacyRepository termsPrivacyRepository;
    private final FileStorageService fileStorageService;

    public AdminContentService(FaqRepository faqRepository, TestimonyRepository testimonyRepository,
                                CmsImageRepository cmsImageRepository, ContentRepository contentRepository,
                                TermsPrivacyRepository termsPrivacyRepository, FileStorageService fileStorageService) {
        this.faqRepository = faqRepository;
        this.testimonyRepository = testimonyRepository;
        this.cmsImageRepository = cmsImageRepository;
        this.contentRepository = contentRepository;
        this.termsPrivacyRepository = termsPrivacyRepository;
        this.fileStorageService = fileStorageService;
    }

    // FAQ
    @Transactional(readOnly = true)
    public List<AdminFaqSummary> listFaqs() {
        return faqRepository.findAll().stream().map(AdminFaqSummary::from).toList();
    }

    @Transactional
    public AdminFaqSummary createFaq(AdminFaqRequest r) {
        Faq faq = Faq.builder().refKey(randomKey()).question(r.question()).answer(r.answer())
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
        return AdminFaqSummary.from(faqRepository.save(faq));
    }

    @Transactional
    public AdminFaqSummary updateFaq(Long id, AdminFaqRequest r) {
        Faq faq = faqRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "FAQ not found."));
        faq.setQuestion(r.question());
        faq.setAnswer(r.answer());
        faq.setUpdatedAt(LocalDateTime.now());
        return AdminFaqSummary.from(faqRepository.save(faq));
    }

    @Transactional
    public void deleteFaq(Long id) {
        faqRepository.deleteById(id);
    }

    // Testimonials
    @Transactional(readOnly = true)
    public List<AdminTestimonySummary> listTestimonials() {
        return testimonyRepository.findAll().stream().map(AdminTestimonySummary::from).toList();
    }

    @Transactional
    public AdminTestimonySummary createTestimonial(AdminTestimonyRequest r) {
        Testimony t = Testimony.builder().refKey(randomKey()).name(r.name()).position(r.position())
                .whatIsSaid(r.whatIsSaid()).picture(r.picture())
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
        return AdminTestimonySummary.from(testimonyRepository.save(t));
    }

    @Transactional
    public AdminTestimonySummary updateTestimonial(Long id, AdminTestimonyRequest r) {
        Testimony t = testimonyRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Testimonial not found."));
        t.setName(r.name());
        t.setPosition(r.position());
        t.setWhatIsSaid(r.whatIsSaid());
        t.setPicture(r.picture());
        t.setUpdatedAt(LocalDateTime.now());
        return AdminTestimonySummary.from(testimonyRepository.save(t));
    }

    @Transactional
    public void deleteTestimonial(Long id) {
        testimonyRepository.deleteById(id);
    }

    // CMS images (no delete in source)
    @Transactional(readOnly = true)
    public List<AdminCmsImageSummary> listImages() {
        return cmsImageRepository.findAll().stream().map(AdminCmsImageSummary::from).toList();
    }

    @Transactional
    public AdminCmsImageSummary createImage(String title, String description, MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "An image file is required.");
        }
        CmsImage img = CmsImage.builder().refKey(randomKey()).title(title).description(description)
                .imagePath(fileStorageService.storeImage(image, "photos", ALLOWED_IMAGE_EXTENSIONS))
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
        return AdminCmsImageSummary.from(cmsImageRepository.save(img));
    }

    @Transactional
    public AdminCmsImageSummary updateImage(Long id, String title, String description, MultipartFile image) {
        CmsImage img = cmsImageRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Image not found."));
        img.setTitle(title);
        img.setDescription(description);
        if (image != null && !image.isEmpty()) {
            fileStorageService.delete(img.getImagePath());
            img.setImagePath(fileStorageService.storeImage(image, "photos", ALLOWED_IMAGE_EXTENSIONS));
        }
        img.setUpdatedAt(LocalDateTime.now());
        return AdminCmsImageSummary.from(cmsImageRepository.save(img));
    }

    // Page content (no delete in source)
    @Transactional(readOnly = true)
    public List<AdminContentSummary> listContents() {
        return contentRepository.findAll().stream().map(AdminContentSummary::from).toList();
    }

    @Transactional
    public AdminContentSummary createContent(AdminContentRequest r) {
        Content c = Content.builder().refKey(randomKey()).title(r.title()).description(r.description())
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
        return AdminContentSummary.from(contentRepository.save(c));
    }

    @Transactional
    public AdminContentSummary updateContent(Long id, AdminContentRequest r) {
        Content c = contentRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Content not found."));
        c.setTitle(r.title());
        c.setDescription(r.description());
        c.setUpdatedAt(LocalDateTime.now());
        return AdminContentSummary.from(contentRepository.save(c));
    }

    // Terms & Privacy (single row, id=1)
    @Transactional(readOnly = true)
    public TermsPrivacy getTermsPrivacy() {
        return termsPrivacyRepository.findById(1L)
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Terms & Privacy row is missing."));
    }

    @Transactional
    public TermsPrivacy updateTermsPrivacy(AdminTermsPrivacyRequest r) {
        TermsPrivacy terms = getTermsPrivacy();
        terms.setDescription(r.description());
        terms.setUseTerms(r.useTerms());
        terms.setUpdatedAt(LocalDateTime.now());
        return termsPrivacyRepository.save(terms);
    }

    private String randomKey() {
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
