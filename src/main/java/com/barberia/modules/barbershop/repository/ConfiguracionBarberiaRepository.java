package com.barberia.modules.barbershop.repository;

import com.barberia.modules.barbershop.entity.ConfiguracionBarberia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfiguracionBarberiaRepository extends JpaRepository<ConfiguracionBarberia, Integer> {
}
