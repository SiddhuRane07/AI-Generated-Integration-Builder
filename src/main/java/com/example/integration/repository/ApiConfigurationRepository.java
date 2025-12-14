package com.example.integration.repository;

import com.example.integration.model.ApiConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiConfigurationRepository extends JpaRepository<ApiConfiguration, Long> {
    
    Optional<ApiConfiguration> findBySystemNameAndActiveTrue(String systemName);
    
    List<ApiConfiguration> findByActiveTrue();
}
