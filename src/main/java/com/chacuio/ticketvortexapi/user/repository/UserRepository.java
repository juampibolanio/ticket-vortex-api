package com.chacuio.ticketvortexapi.user.repository;

import com.chacuio.ticketvortexapi.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmail(String email);
    boolean existsByDocumentNumber(String documentNumber);
    boolean existsByEmailAndIdNot(String email, UUID id);
    boolean existsByDocumentNumberAndIdNot(String documentNumber, UUID id);
}
