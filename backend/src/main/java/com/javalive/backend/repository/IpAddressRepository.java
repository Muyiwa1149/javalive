package com.javalive.backend.repository;

import com.javalive.backend.entity.IpAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IpAddressRepository extends JpaRepository<IpAddress, Long> {

    Optional<IpAddress> findByIpAddress(String ipAddress);
}
