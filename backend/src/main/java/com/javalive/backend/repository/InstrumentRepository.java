package com.javalive.backend.repository;

import com.javalive.backend.entity.Instrument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstrumentRepository extends JpaRepository<Instrument, Long> {
}
