package com.barberia.modules.icloud.repository;

import com.barberia.modules.icloud.entity.IcloudConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IcloudConfigRepository extends JpaRepository<IcloudConfig, Integer> {
}
