package com.javalive.backend.web.admin;

import com.javalive.backend.dto.admin.AdminAddIpRequest;
import com.javalive.backend.dto.admin.AdminIpAddressSummary;
import com.javalive.backend.service.settings.IpBlacklistService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Mirrors source's {@code Admin\IpaddressController} — but returns real JSON instead of an HTML-string field. */
@RestController
@RequestMapping("/api/admin/ip-blacklist")
public class AdminIpAddressController {

    private final IpBlacklistService ipBlacklistService;

    public AdminIpAddressController(IpBlacklistService ipBlacklistService) {
        this.ipBlacklistService = ipBlacklistService;
    }

    @GetMapping
    public List<AdminIpAddressSummary> list() {
        return ipBlacklistService.list().stream().map(AdminIpAddressSummary::from).toList();
    }

    @PostMapping
    public AdminIpAddressSummary add(@Valid @RequestBody AdminAddIpRequest request) {
        return AdminIpAddressSummary.from(ipBlacklistService.add(request.ipAddress()));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        ipBlacklistService.remove(id);
    }
}
