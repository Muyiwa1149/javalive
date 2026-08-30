package com.javalive.backend.repository;

import com.javalive.backend.entity.Instrument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InstrumentRepository extends JpaRepository<Instrument, Long> {

    List<Instrument> findAllByOrderByVolumeDescMarketCapDesc();

    List<Instrument> findByTypeOrderByVolumeDescMarketCapDesc(String type);

    Optional<Instrument> findBySymbol(String symbol);
}
