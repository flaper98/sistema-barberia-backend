package com.barberia.modules.services.repository;

import com.barberia.modules.services.entity.ServicioCortesia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServicioCortesiaRepository extends JpaRepository<ServicioCortesia, Long> {

    List<ServicioCortesia> findByServicioId(Integer servicioId);

    void deleteByServicioId(Integer servicioId);
}
