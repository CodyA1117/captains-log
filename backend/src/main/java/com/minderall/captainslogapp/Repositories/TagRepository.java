package com.minderall.captainslogapp.Repositories;

import com.minderall.captainslogapp.Models.Tag;
import com.minderall.captainslogapp.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByNameAndUser(String name, User user);
    List<Tag> findByUser(User user);
}