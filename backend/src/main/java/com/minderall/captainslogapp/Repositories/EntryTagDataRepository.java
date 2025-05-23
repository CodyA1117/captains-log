package com.minderall.captainslogapp.Repositories;

import com.minderall.captainslogapp.Models.EntryTagData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EntryTagDataRepository extends JpaRepository<EntryTagData, Long> {
    // Custom methods if needed, e.g., find by Entry and Tag
}