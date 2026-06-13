package com.balbuena.Scout.service;

import com.balbuena.Scout.dto.Request;
import com.balbuena.Scout.dto.Response;
import com.balbuena.Scout.exception.ScoutException;
import com.balbuena.Scout.model.*;
import com.balbuena.Scout.repository.PresidentRepository;
import com.balbuena.Scout.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferService {

    private final PresidentRepository presidentRepository;
    private final PlayerRepository playerRepository;
    private final GameService gameService;
    private final PresidentService presidentService;

    @Transactional
    public Response.President swapWithAvailablePlayer(Request.Transfer request) {
        gameService.validatePhase(GamePhase.TRANSFER_WINDOW);

        President president = presidentService.getPresident(request.getPresidentId());
        validateTransferAllowed(president);

        Player playerOut = getOwnedPlayer(president, request.getPlayerOutId());
        Player playerIn  = playerRepository.findById(request.getPlayerInId())
                .orElseThrow(() -> new ScoutException("Player not found: ID " + request.getPlayerInId()));

        if (!playerIn.isAvailable()) {
            throw new ScoutException("Player " + playerIn.getName() + " is not available");
        }

        validateGoalkeeperRule(president, playerOut, playerIn);

        double diff = playerIn.getValue() - playerOut.getValue();
        if (diff > 0 && president.getBudget() < diff) {
            throw new ScoutException(String.format(
                "Insufficient funds. Difference: BRL %.2f | Available: BRL %.2f", diff, president.getBudget()));
        }

        playerOut.setPresident(null);
        playerOut.setAvailable(true);
        playerRepository.save(playerOut);

        playerIn.setPresident(president);
        playerIn.setAvailable(false);
        playerRepository.save(playerIn);

        president.setBudget(president.getBudget() - diff);
        president.setUsedBudget(president.getUsedBudget() + diff);
        president.setTransferUsed(true);
        presidentRepository.save(president);

        log.info("{} swapped {} for {}", president.getName(), playerOut.getName(), playerIn.getName());
        return presidentService.toResponse(president);
    }

    @Transactional
    public Response.President negotiateWithPresident(Request.Transfer request) {
        gameService.validatePhase(GamePhase.TRANSFER_WINDOW);

        President buyer  = presidentService.getPresident(request.getPresidentId());
        validateTransferAllowed(buyer);

        if (request.getTargetPresidentId() == null || request.getOfferAmount() == null) {
            throw new ScoutException("targetPresidentId and offerAmount are required for negotiations");
        }

        President seller = presidentService.getPresident(request.getTargetPresidentId());
        Player playerOut  = getOwnedPlayer(buyer, request.getPlayerOutId());
        Player playerIn   = getOwnedPlayer(seller, request.getPlayerInId());

        if (buyer.getBudget() < request.getOfferAmount()) {
            throw new ScoutException(String.format(
                "Insufficient funds. Offer: BRL %.2f | Available: BRL %.2f",
                request.getOfferAmount(), buyer.getBudget()));
        }

        validateGoalkeeperRule(buyer,  playerOut, playerIn);
        validateGoalkeeperRule(seller, playerIn,  playerOut);

        playerOut.setPresident(seller); playerRepository.save(playerOut);
        playerIn.setPresident(buyer);   playerRepository.save(playerIn);

        buyer.setBudget(buyer.getBudget() - request.getOfferAmount());
        buyer.setUsedBudget(buyer.getUsedBudget() + request.getOfferAmount());
        buyer.setTransferUsed(true);
        presidentRepository.save(buyer);

        seller.setBudget(seller.getBudget() + request.getOfferAmount());
        presidentRepository.save(seller);

        log.info("{} bought {} from {} for BRL {}", buyer.getName(), playerIn.getName(), seller.getName(), request.getOfferAmount());
        return presidentService.toResponse(buyer);
    }

    public List<Response.Player> getAvailableForTransfer() {
        return playerRepository.findByAvailableTrue().stream()
                .map(p -> Response.Player.builder()
                        .id(p.getId()).name(p.getName()).position(p.getPosition())
                        .value(p.getValue()).available(true).auctionPlayer(p.isAuctionPlayer())
                        .goalsScored(p.getGoalsScored()).goalsConceded(p.getGoalsConceded()).build())
                .collect(Collectors.toList());
    }

    private void validateTransferAllowed(President president) {
        if (president.isTransferUsed()) {
            throw new ScoutException("This club has already used its transfer in this window");
        }
    }

    private Player getOwnedPlayer(President president, Long playerId) {
        return president.getTeam().stream().filter(p -> p.getId().equals(playerId)).findFirst()
                .orElseThrow(() -> new ScoutException(
                    "Player ID " + playerId + " does not belong to " + president.getClubName()));
    }

    private void validateGoalkeeperRule(President president, Player out, Player in) {
        if (out.getPosition() == Position.GOALKEEPER && in.getPosition() != Position.GOALKEEPER) {
            long otherGks = president.getTeam().stream()
                    .filter(p -> p.getPosition() == Position.GOALKEEPER && !p.getId().equals(out.getId())).count();
            if (otherGks == 0) {
                throw new ScoutException("The only goalkeeper cannot be removed without adding another goalkeeper");
            }
        }
    }
}
