package com.ziyad.courselens.repository;

import com.ziyad.courselens.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository {

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
