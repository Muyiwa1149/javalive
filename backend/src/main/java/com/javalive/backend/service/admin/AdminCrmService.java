package com.javalive.backend.service.admin;

import com.javalive.backend.dto.admin.AdminLeadSummary;
import com.javalive.backend.dto.admin.AdminPickerSummary;
import com.javalive.backend.dto.admin.AdminTaskRequest;
import com.javalive.backend.dto.admin.AdminTaskSummary;
import com.javalive.backend.entity.Admin;
import com.javalive.backend.entity.Task;
import com.javalive.backend.entity.User;
import com.javalive.backend.repository.AdminRepository;
import com.javalive.backend.repository.TaskRepository;
import com.javalive.backend.repository.UserRepository;
import com.javalive.backend.service.mail.MailService;
import com.javalive.backend.web.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** Mirrors the source app's {@code Admin\CrmController} + the task/leads views on {@code HomeController}. */
@Service
public class AdminCrmService {

    private final TaskRepository taskRepository;
    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final MailService mailService;

    public AdminCrmService(TaskRepository taskRepository, AdminRepository adminRepository, UserRepository userRepository,
                            MailService mailService) {
        this.taskRepository = taskRepository;
        this.adminRepository = adminRepository;
        this.userRepository = userRepository;
        this.mailService = mailService;
    }

    @Transactional(readOnly = true)
    public List<AdminPickerSummary> adminPicker() {
        return adminRepository.findAll().stream().map(AdminPickerSummary::from).toList();
    }

    @Transactional(readOnly = true)
    public List<AdminTaskSummary> allTasks() {
        return taskRepository.findAllWithAdminOrderByIdDesc().stream().map(AdminTaskSummary::from).toList();
    }

    @Transactional(readOnly = true)
    public List<AdminTaskSummary> myTasks(Long adminId) {
        return taskRepository.findByAssignedToAdminIdOrderByIdDesc(adminId).stream().map(AdminTaskSummary::from).toList();
    }

    @Transactional
    public AdminTaskSummary createTask(AdminTaskRequest request) {
        Admin assignee = adminRepository.findById(request.assignedAdminId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Selected admin not found."));

        Task task = Task.builder()
                .title(request.title()).note(request.note()).assignedToAdmin(assignee)
                .startDate(request.startDate()).endDate(request.endDate()).priority(request.priority())
                .status("Pending")
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
        task = taskRepository.save(task);

        mailService.send(assignee.getEmail(), "New Task: " + request.title(),
                "This is to inform you that a new task has been assigned to you, Task Title: " + request.title()
                        + ", Start Date: " + request.startDate() + ", End Date: " + request.endDate()
                        + ", please login to your account to see more.");

        return AdminTaskSummary.from(task);
    }

    @Transactional
    public AdminTaskSummary updateTask(Long id, AdminTaskRequest request) {
        Task task = getTask(id);
        Admin assignee = adminRepository.findById(request.assignedAdminId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Selected admin not found."));

        task.setTitle(request.title());
        task.setNote(request.note());
        task.setAssignedToAdmin(assignee);
        task.setStartDate(request.startDate());
        task.setEndDate(request.endDate());
        task.setPriority(request.priority());
        task.setUpdatedAt(LocalDateTime.now());
        return AdminTaskSummary.from(taskRepository.save(task));
    }

    @Transactional
    public void markDone(Long id) {
        Task task = getTask(id);
        task.setStatus("Completed");
        task.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(task);
    }

    @Transactional
    public void deleteTask(Long id) {
        taskRepository.delete(getTask(id));
    }

    /** Mirrors source's {@code HomeController@leads} — cstatus IS NULL means not yet a converted customer. */
    @Transactional(readOnly = true)
    public List<AdminLeadSummary> leads() {
        return userRepository.findByCustomerStatusIsNullOrderByIdDesc().stream().map(AdminLeadSummary::from).toList();
    }

    @Transactional
    public void convertLead(Long userId) {
        User user = getUser(userId);
        user.setCustomerStatus("Customer");
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional
    public void assignLead(Long userId, Long adminId) {
        User user = getUser(userId);
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Selected admin not found."));
        user.setAssignedAgent(String.valueOf(adminId));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        mailService.send(admin.getEmail(), "New User Assigned",
                "This is to inform you that a user have been assigned to you, please login to your account for more info");
    }

    private Task getTask(Long id) {
        return taskRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Task not found."));
    }

    private User getUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found."));
    }
}
