package com.part3.team07.sb01deokhugamteam07.repository;

//import com.part3.team07.sb01deokhugamteam07.dto.notification.UUID;
import com.part3.team07.sb01deokhugamteam07.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

  List<User> findByIsDeletedFalse();
  boolean existsByEmail(String email);
  boolean existsById(UUID id);

  Optional<User> findByEmail(String email);
}
