package com.minderall.captainslogapp.Repositories;

import com.minderall.captainslogapp.Models.OuraData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OuraDataRepository extends JpaRepository<OuraData, Long> {

}
