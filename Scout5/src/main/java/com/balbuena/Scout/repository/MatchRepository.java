package com.balbuena.Scout.repository;

import com.balbuena.Scout.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByRoundNumber(int roundNumber);
    List<Match> findByRoundNumberAndPlayedFalse(int roundNumber);
    List<Match> findByHomePresidentIdOrAwayPresidentId(Long homeId, Long awayId);
    long countByPlayedFalse();
}
