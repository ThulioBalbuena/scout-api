package com.balbuena.Scout.dto;

import com.balbuena.Scout.model.GamePhase;
import com.balbuena.Scout.model.Position;
import lombok.*;

import java.util.List;

public class Response {

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Player {
        public Long id;
        public String name;
        public Position position;
        public Double value;
        public boolean auctionPlayer;
        public boolean available;
        public int goalsScored;
        public int goalsConceded;
        public String presidentName;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class President {
        public Long id;
        public String name;
        public String email;
        public String clubName;
        public Double budget;
        public Double usedBudget;
        public boolean teamComplete;
        public boolean hasGoalkeeper;
        public List<Player> team;
        public int points;
        public int wins;
        public int draws;
        public int losses;
        public int goalsFor;
        public int goalsAgainst;
        public int goalDifference;
        public boolean transferUsed;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Match {
        public Long id;
        public int roundNumber;
        public String homePresident;
        public String awayPresident;
        public String homeClub;
        public String awayClub;
        public Integer homeGoals;
        public Integer awayGoals;
        public boolean played;
        public String result;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class GameState {
        public GamePhase phase;
        public String phaseDescription;
        public int currentRound;
        public int totalRounds;
        public int currentAuctionPlayerIndex;
        public String currentAuctionPlayerName;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Standing {
        public int position;
        public String presidentName;
        public String clubName;
        public int points;
        public int wins;
        public int draws;
        public int losses;
        public int goalsFor;
        public int goalsAgainst;
        public int goalDifference;
        public int matchesPlayed;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class TopScorer {
        public int rank;
        public String playerName;
        public Position position;
        public String presidentName;
        public int goals;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ChampionshipReport {
        public List<Standing> standings;
        public List<TopScorer> topScorers;
        public Standing bestAttack;
        public Standing bestDefense;
        public String champion;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AuctionStatus {
        public Long playerId;
        public String playerName;
        public Position playerPosition;
        public Double playerBaseValue;
        public Double currentHighestBid;
        public String currentLeader;
        public List<BidInfo> bids;

        @Data @NoArgsConstructor @AllArgsConstructor @Builder
        public static class BidInfo {
            public String presidentName;
            public Double bidAmount;
            public String bidTime;
        }
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ApiMessage {
        public String message;
        public Object data;

        public static ApiMessage of(String message) {
            return ApiMessage.builder().message(message).build();
        }

        public static ApiMessage of(String message, Object data) {
            return ApiMessage.builder().message(message).data(data).build();
        }
    }
}
