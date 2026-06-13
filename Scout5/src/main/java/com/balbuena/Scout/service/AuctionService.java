package com.balbuena.Scout.service;

import com.balbuena.Scout.dto.Request;
import com.balbuena.Scout.dto.Response;
import com.balbuena.Scout.exception.ScoutException;
import com.balbuena.Scout.model.*;
import com.balbuena.Scout.repository.AuctionBidRepository;
import com.balbuena.Scout.repository.PresidentRepository;
import com.balbuena.Scout.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionService {

    private final AuctionBidRepository bidRepository;
    private final PresidentRepository presidentRepository;
    private final PlayerRepository playerRepository;
    private final GameService gameService;
    private final PresidentService presidentService;

    public Response.AuctionStatus getCurrentAuctionStatus() {
        gameService.validatePhase(GamePhase.DRAFT_AUCTION);
        Player player = getCurrentAuctionPlayer();
        return buildAuctionStatus(player);
    }

    @Transactional
    public Response.AuctionStatus placeBid(Long playerId, Request.PlaceBid request) {
        gameService.validatePhase(GamePhase.DRAFT_AUCTION);

        Player player = getCurrentAuctionPlayer();
        if (!player.getId().equals(playerId)) {
            throw new ScoutException("The player currently up for auction is " + player.getName()
                    + " (ID: " + player.getId() + ")");
        }

        President president = presidentService.getPresident(request.getPresidentId());

        if (president.isTeamComplete()) {
            throw new ScoutException("This squad is already complete with 5 players");
        }
        if (request.getBidAmount() > president.getBudget()) {
            throw new ScoutException(String.format(
                "Insufficient funds. Available: BRL %.2f | Bid: BRL %.2f",
                president.getBudget(), request.getBidAmount()));
        }

        Optional<AuctionBid> currentHighest = bidRepository.findTopByPlayerIdOrderByBidAmountDesc(playerId);
        if (currentHighest.isPresent()) {
            if (request.getBidAmount() <= currentHighest.get().getBidAmount()) {
                throw new ScoutException(String.format(
                    "The bid must be higher than the current BRL %.2f", currentHighest.get().getBidAmount()));
            }
            if (currentHighest.get().getPresident().getId().equals(president.getId())) {
                throw new ScoutException("This president already has the highest bid");
            }
        } else if (request.getBidAmount() < player.getValue()) {
            throw new ScoutException(String.format(
                "The minimum bid is the player's base value: BRL %.2f", player.getValue()));
        }

        AuctionBid bid = AuctionBid.builder()
                .player(player)
                .president(president)
                .bidAmount(request.getBidAmount())
                .bidTime(LocalDateTime.now())
                .build();
        bidRepository.save(bid);

        log.info("Bid of BRL {} placed by {} for {}", request.getBidAmount(), president.getName(), player.getName());
        return buildAuctionStatus(player);
    }

    @Transactional
    public Response.AuctionStatus finalizeCurrentAuction() {
        gameService.validatePhase(GamePhase.DRAFT_AUCTION);
        Player player = getCurrentAuctionPlayer();

        Optional<AuctionBid> winnerBid = bidRepository.findTopByPlayerIdOrderByBidAmountDesc(player.getId());

        if (winnerBid.isPresent()) {
            AuctionBid winner = winnerBid.get();
            winner.setWinningBid(true);
            bidRepository.save(winner);

            President president = winner.getPresident();
            president.setBudget(president.getBudget() - winner.getBidAmount());
            president.setUsedBudget(president.getUsedBudget() + winner.getBidAmount());
            presidentRepository.save(president);

            player.setAvailable(false);
            player.setPresident(president);
            playerRepository.save(player);

            log.info("{} won {} for BRL {}", president.getName(), player.getName(), winner.getBidAmount());
        } else {
            player.setAuctionPlayer(false);
            playerRepository.save(player);
            log.info("No bids for {}. Player moved to the lottery", player.getName());
        }

        gameService.advanceAuctionToNextPlayer();
        return buildAuctionStatus(player);
    }

    private Player getCurrentAuctionPlayer() {
        GameState state = gameService.getGameState();
        if (state.getCurrentAuctionPlayerIndex() >= GameService.AUCTION_PLAYER_NAMES.size()) {
            throw new ScoutException("All auction players have already been processed");
        }

        String playerName = GameService.AUCTION_PLAYER_NAMES.get(state.getCurrentAuctionPlayerIndex());
        return playerRepository.findByName(playerName)
                .orElseThrow(() -> new ScoutException("Auction player was not found: " + playerName));
    }

    private Response.AuctionStatus buildAuctionStatus(Player player) {
        List<AuctionBid> bids = bidRepository.findByPlayerIdOrderByBidAmountDesc(player.getId());
        Optional<AuctionBid> highest = bids.isEmpty() ? Optional.empty() : Optional.of(bids.get(0));

        List<Response.AuctionStatus.BidInfo> bidInfos = bids.stream()
                .map(b -> Response.AuctionStatus.BidInfo.builder()
                        .presidentName(b.getPresident().getName())
                        .bidAmount(b.getBidAmount())
                        .bidTime(b.getBidTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")))
                        .build())
                .collect(Collectors.toList());

        return Response.AuctionStatus.builder()
                .playerId(player.getId())
                .playerName(player.getName())
                .playerPosition(player.getPosition())
                .playerBaseValue(player.getValue())
                .currentHighestBid(highest.map(AuctionBid::getBidAmount).orElse(null))
                .currentLeader(highest.map(b -> b.getPresident().getName()).orElse(null))
                .bids(bidInfos)
                .build();
    }
}
