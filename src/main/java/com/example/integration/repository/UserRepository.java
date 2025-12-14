package com.example.integration.repository;

import com.example.integration.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    List<User> findBySystemName(String systemName);
    
    Optional<User> findBySystemNameAndExternalId(String systemName, String externalId);
    
    void deleteBySystemName(String systemName);
}
