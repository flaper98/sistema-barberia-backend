package com.barberia.modules.loyalty.repository;

import com.barberia.modules.loyalty.entity.Recompensa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecompensaRepository extends JpaRepository<Recompensa, Integer> {

    List<Recompensa> findByActivoTrue();
}
