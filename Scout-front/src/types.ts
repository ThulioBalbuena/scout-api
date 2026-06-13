export type GamePhase =
  | "REGISTRATION"
  | "DRAFT_AUCTION"
  | "DRAFT_LOTTERY"
  | "CHAMPIONSHIP"
  | "TRANSFER_WINDOW"
  | "FINISHED";

export type Position = "GOALKEEPER" | "DEFENDER" | "MIDFIELDER" | "FORWARD";

export interface Player {
  id: number;
  name: string;
  position: Position;
  value: number;
  auctionPlayer: boolean;
  available: boolean;
  goalsScored: number;
  goalsConceded: number;
  presidentName?: string;
}

export interface President {
  id: number;
  name: string;
  email: string;
  clubName: string;
  budget: number;
  usedBudget: number;
  teamComplete: boolean;
  hasGoalkeeper: boolean;
  team: Player[];
  points: number;
  wins: number;
  draws: number;
  losses: number;
  goalsFor: number;
  goalsAgainst: number;
  goalDifference: number;
  transferUsed: boolean;
}

export interface Match {
  id: number;
  roundNumber: number;
  homePresident: string;
  awayPresident: string;
  homeClub?: string;
  awayClub?: string;
  homeGoals: number | null;
  awayGoals: number | null;
  played: boolean;
  result?: string;
}

export interface GameState {
  phase: GamePhase;
  phaseDescription: string;
  currentRound: number;
  totalRounds: number;
  currentAuctionPlayerIndex: number;
  currentAuctionPlayerName?: string;
}

export interface AuctionStatus {
  playerId: number;
  playerName: string;
  playerPosition: Position;
  playerBaseValue: number;
  currentHighestBid: number;
  currentLeader?: string;
  bids: Array<{
    presidentName: string;
    bidAmount: number;
    bidTime: string;
  }>;
}

export interface Standing {
  position: number;
  presidentName: string;
  clubName: string;
  points: number;
  wins: number;
  draws: number;
  losses: number;
  goalsFor: number;
  goalsAgainst: number;
  goalDifference: number;
  matchesPlayed: number;
}

export interface TopScorer {
  rank: number;
  playerName: string;
  position: Position;
  presidentName: string;
  goals: number;
}

export interface ChampionshipReport {
  standings: Standing[];
  topScorers: TopScorer[];
  bestAttack?: Standing;
  bestDefense?: Standing;
  champion?: string;
}

export interface ApiMessage<T = unknown> {
  message: string;
  data?: T;
}
