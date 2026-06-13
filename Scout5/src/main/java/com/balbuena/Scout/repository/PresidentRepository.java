package com.balbuena.Scout.repository;

import com.balbuena.Scout.model.President;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PresidentRepository extends JpaRepository<President, Long> {
    Optional<President> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByName(String name);
    boolean existsByClubName(String clubName);

    @Query("SELECT p FROM President p ORDER BY p.points DESC, (p.goalsFor - p.goalsAgainst) DESC, p.goalsFor DESC")
    List<President> findAllOrderedByStandings();
}
