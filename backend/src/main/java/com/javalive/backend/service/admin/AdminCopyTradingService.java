package com.javalive.backend.service.admin;

import com.javalive.backend.dto.admin.AdminCopyTradeSummary;
import com.javalive.backend.dto.admin.AdminCopyTradingStats;
import com.javalive.backend.dto.admin.AdminExpertRequest;
import com.javalive.backend.dto.admin.AdminExpertSummary;
import com.javalive.backend.entity.CopyTradingExpert;
import com.javalive.backend.repository.CopyTradingExpertRepository;
import com.javalive.backend.repository.UserCopyTradeRepository;
import com.javalive.backend.service.storage.FileStorageService;
import com.javalive.backend.web.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/** Mirrors the source app's new/canonical {@code CopyTradingAdminController}, per CANONICAL-MODULES.md. */
@Service
public class AdminCopyTradingService {

    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = List.of("jpg", "jpeg", "png", "gif");

    private final CopyTradingExpertRepository expertRepository;
    private final UserCopyTradeRepository copyTradeRepository;
    private final FileStorageService fileStorageService;

    public AdminCopyTradingService(CopyTradingExpertRepository expertRepository, UserCopyTradeRepository copyTradeRepository,
                                    FileStorageService fileStorageService) {
        this.expertRepository = expertRepository;
        this.copyTradeRepository = copyTradeRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional(readOnly = true)
    public List<AdminExpertSummary> list() {
        return expertRepository.findAllByOrderByIdDesc().stream()
                .map(e -> AdminExpertSummary.from(e, copyTradeRepository.countByExpertId(e.getId()),
                        copyTradeRepository.countResolvableByExpertIdAndActive(e.getId(), "yes")))
                .toList();
    }

    @Transactional
    public AdminExpertSummary create(AdminExpertRequest request, MultipartFile photo) {
        CopyTradingExpert expert = CopyTradingExpert.builder()
                .name(request.name()).tag(request.tag()).rating(request.rating()).followers(0)
                .equity(request.equity()).totalProfit(request.totalProfit()).status(request.status())
                .description(request.description()).winRate(request.winRate()).totalTrades(request.totalTrades())
                .price(request.price()).type("Main")
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
        if (photo != null && !photo.isEmpty()) {
            expert.setPhoto(fileStorageService.storeImage(photo, "copy-experts", ALLOWED_IMAGE_EXTENSIONS));
        }
        expert = expertRepository.save(expert);
        return AdminExpertSummary.from(expert, 0, 0);
    }

    @Transactional
    public AdminExpertSummary update(Long id, AdminExpertRequest request, MultipartFile photo) {
        CopyTradingExpert expert = getExpert(id);
        expert.setName(request.name());
        expert.setTag(request.tag());
        expert.setRating(request.rating());
        expert.setEquity(request.equity());
        expert.setTotalProfit(request.totalProfit());
        expert.setStatus(request.status());
        expert.setDescription(request.description());
        expert.setWinRate(request.winRate());
        expert.setTotalTrades(request.totalTrades());
        expert.setPrice(request.price());
        expert.setUpdatedAt(LocalDateTime.now());
        if (photo != null && !photo.isEmpty()) {
            fileStorageService.delete(expert.getPhoto());
            expert.setPhoto(fileStorageService.storeImage(photo, "copy-experts", ALLOWED_IMAGE_EXTENSIONS));
        }
        expert = expertRepository.save(expert);
        return AdminExpertSummary.from(expert, copyTradeRepository.countByExpertId(id),
                copyTradeRepository.countResolvableByExpertIdAndActive(id, "yes"));
    }

    /**
     * The delete-guard deliberately uses the raw (non-join) count, not the "resolvable" one used
     * for display — it must block deletion on *any* row referencing this expert as active,
     * including an orphaned one, or the delete would leave a dangling FK.
     */
    @Transactional
    public void delete(Long id) {
        CopyTradingExpert expert = getExpert(id);
        long activeCopiers = copyTradeRepository.countByExpertIdAndActive(id, "yes");
        if (activeCopiers > 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot delete expert trader with active copiers.");
        }
        fileStorageService.delete(expert.getPhoto());
        expertRepository.delete(expert);
    }

    @Transactional(readOnly = true)
    public List<AdminCopyTradeSummary> activeTrades() {
        return copyTradeRepository.findByActiveWithUserAndExpertOrderByCreatedAtDesc("yes")
                .stream().map(AdminCopyTradeSummary::from).toList();
    }

    @Transactional(readOnly = true)
    public AdminCopyTradingStats statistics() {
        return new AdminCopyTradingStats(
                expertRepository.count(), expertRepository.countByStatus("active"),
                copyTradeRepository.count(), copyTradeRepository.countResolvableByActive("yes"),
                copyTradeRepository.sumPriceByActive("yes"), copyTradeRepository.sumTotalProfitByActive("yes"),
                copyTradeRepository.countDistinctUsersByActive("yes")
        );
    }

    private CopyTradingExpert getExpert(Long id) {
        return expertRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Expert trader not found."));
    }
}
