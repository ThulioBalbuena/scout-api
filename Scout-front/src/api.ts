import type {
  ApiMessage,
  AuctionStatus,
  ChampionshipReport,
  GameState,
  Match,
  Player,
  President,
  Standing,
  TopScorer
} from "./types";

const API_BASE = import.meta.env.VITE_API_BASE_URL ?? "";

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, {
    headers: {
      "Content-Type": "application/json",
      ...options.headers
    },
    ...options
  });

  const text = await response.text();
  const contentType = response.headers.get("content-type") ?? "";
  let data: unknown;

  if (text) {
    if (!contentType.toLowerCase().includes("json")) {
      throw new Error(
        `The API endpoint ${path} returned ${contentType || "a non-JSON response"} (${response.status}). ` +
          "Check whether the Spring backend and the frontend proxy are running."
      );
    }

    try {
      data = JSON.parse(text);
    } catch {
      throw new Error(`The API endpoint ${path} returned invalid JSON.`);
    }
  }

  if (!response.ok) {
    const payload = data as { message?: string; error?: string } | undefined;
    const message = payload?.message ?? payload?.error ?? `Request failed (${response.status})`;
    throw new Error(message);
  }

  return data as T;
}

export const api = {
  getGameState: () => request<GameState>("/api/game/state"),
  startAuction: () => request<ApiMessage<GameState>>("/api/game/start-auction", { method: "POST" }),
  advanceAuction: () => request<ApiMessage<GameState>>("/api/game/advance-auction", { method: "POST" }),
  startChampionship: () => request<ApiMessage<GameState>>("/api/game/advance-to-championship", { method: "POST" }),
  openTransferWindow: () => request<ApiMessage<GameState>>("/api/game/open-transfer-window", { method: "POST" }),
  closeTransferWindow: () => request<ApiMessage<GameState>>("/api/game/close-transfer-window", { method: "POST" }),
  finishChampionship: () => request<ApiMessage<GameState>>("/api/game/finish", { method: "POST" }),
  resetSeason: () => request<ApiMessage<GameState>>("/api/game/reset", { method: "POST" }),

  getPresidents: () => request<President[]>("/api/presidents"),
  createPresident: (payload: { name: string; email: string; clubName: string }) =>
    request<President>("/api/presidents", { method: "POST", body: JSON.stringify(payload) }),
  createDefaultPresidents: () => request<President[]>("/api/presidents/defaults", { method: "POST" }),

  getPlayers: () => request<Player[]>("/api/players"),
  getAvailablePlayers: () => request<Player[]>("/api/players/available"),
  getAuctionPlayers: () => request<Player[]>("/api/players/auction"),

  getCurrentAuction: () => request<AuctionStatus>("/api/auction/current"),
  placeBid: (playerId: number, payload: { presidentId: number; bidAmount: number }) =>
    request<AuctionStatus>(`/api/auction/players/${playerId}/bid`, {
      method: "POST",
      body: JSON.stringify(payload)
    }),
  finalizeAuction: (playerId: number) =>
    request<ApiMessage<AuctionStatus>>(`/api/auction/players/${playerId}/finalize`, { method: "POST" }),

  getLotteryAvailable: () => request<Player[]>("/api/lottery/available"),
  runLottery: () => request<ApiMessage<string>>("/api/lottery/run", { method: "POST" }),

  generateSchedule: () => request<ApiMessage<Match[]>>("/api/championship/generate-schedule", { method: "POST" }),
  getMatches: () => request<Match[]>("/api/championship/matches"),
  simulateRound: (roundNumber: number) =>
    request<ApiMessage<Match[]>>(`/api/championship/simulate/round/${roundNumber}`, { method: "POST" }),
  simulateAll: () => request<ApiMessage<Match[]>>("/api/championship/simulate/all", { method: "POST" }),

  getTransferAvailable: () => request<Player[]>("/api/transfers/available"),
  swapWithMarket: (payload: { presidentId: number; playerOutId: number; playerInId: number }) =>
    request<President>("/api/transfers/swap", { method: "POST", body: JSON.stringify(payload) }),
  negotiate: (payload: {
    presidentId: number;
    playerOutId: number;
    playerInId: number;
    targetPresidentId: number;
    offerAmount: number;
  }) => request<President>("/api/transfers/negotiate", { method: "POST", body: JSON.stringify(payload) }),

  getStandings: () => request<Standing[]>("/api/reports/standings"),
  getTopScorers: () => request<TopScorer[]>("/api/reports/top-scorers"),
  getFullReport: () => request<ChampionshipReport>("/api/reports/full-report")
};
