package com.ziyad.courselens.institution.repository;

import com.ziyad.courselens.institution.entity.Institution;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InstitutionRepository extends JpaRepository<Institution, Long> {

    Optional<Institution> findByDomain(String domain);
}
