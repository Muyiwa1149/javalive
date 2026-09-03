package com.javalive.backend.service.settings;

import com.javalive.backend.entity.IpAddress;
import com.javalive.backend.repository.IpAddressRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** In-memory-cached blacklist backing {@link com.javalive.backend.security.ipblock.IpBlacklistFilter}. */
@Service
public class IpBlacklistService {

    private final IpAddressRepository ipAddressRepository;
    private final Set<String> blocked = ConcurrentHashMap.newKeySet();

    public IpBlacklistService(IpAddressRepository ipAddressRepository) {
        this.ipAddressRepository = ipAddressRepository;
    }

    @PostConstruct
    void warmCache() {
        refresh();
    }

    public boolean isBlocked(String ip) {
        return ip != null && blocked.contains(ip);
    }

    public List<IpAddress> list() {
        return ipAddressRepository.findAll();
    }

    public IpAddress add(String ip) {
        IpAddress entry = ipAddressRepository.findByIpAddress(ip).orElseGet(() -> IpAddress.builder()
                .ipAddress(ip).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build());
        IpAddress saved = ipAddressRepository.save(entry);
        blocked.add(ip);
        return saved;
    }

    public void remove(Long id) {
        ipAddressRepository.findById(id).ifPresent(entry -> {
            ipAddressRepository.delete(entry);
            blocked.remove(entry.getIpAddress());
        });
    }

    /** Public so the admin "Clear Cache" utility can force a re-read after an out-of-band DB change. */
    public void refresh() {
        blocked.clear();
        ipAddressRepository.findAll().forEach(ip -> blocked.add(ip.getIpAddress()));
    }
}
