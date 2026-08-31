package com.javalive.backend.service.membership;

import com.javalive.backend.entity.LedgerTransaction;
import com.javalive.backend.entity.User;
import com.javalive.backend.repository.LedgerTransactionRepository;
import com.javalive.backend.repository.UserRepository;
import com.javalive.backend.service.external.OnlineTraderApiClient;
import com.javalive.backend.web.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Proxies the external app.getonlinetrader.pro courses/lessons API — mirrors the source app's
 * User\MembershipController exactly. The source's "modules" toggle for this feature was fully
 * commented out; per the explicit "make hidden features live" decision it's built live here.
 *
 * <p>See {@link OnlineTraderApiClient}'s javadoc: the migrated settings have no configured
 * merchant key, so this has been verified to compile and wire correctly but not exercised against
 * a real upstream response.
 */
@Service
public class MembershipService {

    private final OnlineTraderApiClient apiClient;
    private final UserRepository userRepository;
    private final LedgerTransactionRepository ledgerTransactionRepository;

    public MembershipService(OnlineTraderApiClient apiClient, UserRepository userRepository,
                              LedgerTransactionRepository ledgerTransactionRepository) {
        this.apiClient = apiClient;
        this.userRepository = userRepository;
        this.ledgerTransactionRepository = ledgerTransactionRepository;
    }

    public Map<String, Object> courses() {
        return apiClient.get("/courses", Map.of());
    }

    public Map<String, Object> courseDetails(String courseId) {
        return apiClient.get("/course", Map.of("courseId", courseId));
    }

    public Map<String, Object> myCourseDetails(Long userId, String courseId) {
        return apiClient.get("/user-course", Map.of("courseId", courseId, "clientId", userId));
    }

    public Map<String, Object> myCourses(Long userId) {
        return apiClient.get("/user-courses", Map.of("userId", userId));
    }

    public Map<String, Object> lesson(Long userId, String lessonId, String courseId) {
        Map<String, Object> course = apiClient.get("/course", Map.of("userId", userId, "courseId", courseId));
        Map<String, Object> lesson = apiClient.get("/lesson", Map.of("lessonId", lessonId));
        return Map.of("course", course, "lesson", lesson);
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public Map<String, Object> buyCourse(Long userId, String courseId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found."));

        Map<String, Object> courseResponse = apiClient.get("/course", Map.of("courseId", courseId));
        Map<String, Object> courseData = (Map<String, Object>) ((Map<String, Object>) courseResponse.get("data")).get("course");
        BigDecimal amount = courseData.get("amount") != null ? new BigDecimal(courseData.get("amount").toString()) : BigDecimal.ZERO;

        Map<String, Object> userCourseResponse = apiClient.get("/user-course", Map.of("courseId", courseId, "clientId", userId));
        Object existingCourse = ((Map<String, Object>) userCourseResponse.get("data")).get("course");
        if (existingCourse != null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "You have already purchased this course, you can view it on the my courses page.");
        }

        if (user.getAccountBalance().compareTo(amount) < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "You have insufficient funds in your account balance to make this purchase, please make a deposit.");
        }

        LocalDateTime now = LocalDateTime.now();
        user.setAccountBalance(user.getAccountBalance().subtract(amount));
        user.setUpdatedAt(now);
        userRepository.save(user);

        Map<String, Object> buyResponse = apiClient.post("/buy-course", Map.of("courseId", courseId, "clientId", userId));

        ledgerTransactionRepository.save(LedgerTransaction.builder()
                .user(user).planLabel("Purchase Course").amount(amount).type("Education")
                .createdAt(now).updatedAt(now).build());

        return buyResponse;
    }
}
