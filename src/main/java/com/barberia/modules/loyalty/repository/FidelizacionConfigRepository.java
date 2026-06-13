package com.barberia.modules.loyalty.repository;

import com.barberia.modules.loyalty.entity.FidelizacionConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FidelizacionConfigRepository extends JpaRepository<FidelizacionConfig, Integer> {
}
