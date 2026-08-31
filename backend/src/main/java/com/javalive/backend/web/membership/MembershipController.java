package com.javalive.backend.web.membership;

import com.javalive.backend.security.UserPrincipal;
import com.javalive.backend.service.membership.MembershipService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/membership")
public class MembershipController {

    private final MembershipService membershipService;

    public MembershipController(MembershipService membershipService) {
        this.membershipService = membershipService;
    }

    @GetMapping("/courses")
    public Map<String, Object> courses() {
        return membershipService.courses();
    }

    @GetMapping("/courses/{id}")
    public Map<String, Object> courseDetails(@PathVariable String id) {
        return membershipService.courseDetails(id);
    }

    @GetMapping("/my-courses")
    public Map<String, Object> myCourses(@AuthenticationPrincipal UserPrincipal principal) {
        return membershipService.myCourses(principal.getId());
    }

    @GetMapping("/my-courses/{id}")
    public Map<String, Object> myCourseDetails(@AuthenticationPrincipal UserPrincipal principal, @PathVariable String id) {
        return membershipService.myCourseDetails(principal.getId(), id);
    }

    @GetMapping("/lessons/{lessonId}")
    public Map<String, Object> lesson(@AuthenticationPrincipal UserPrincipal principal, @PathVariable String lessonId,
                                       @RequestParam(required = false) String courseId) {
        return membershipService.lesson(principal.getId(), lessonId, courseId);
    }

    @PostMapping("/courses/{id}/buy")
    public Map<String, Object> buyCourse(@AuthenticationPrincipal UserPrincipal principal, @PathVariable String id) {
        return membershipService.buyCourse(principal.getId(), id);
    }
}
