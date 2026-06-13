import { useEffect, useMemo, useState } from "react";
import {
  BadgeDollarSign,
  CheckCircle2,
  ChevronsRight,
  CircleAlert,
  CircleDollarSign,
  Flag,
  FastForward,
  Shield,
  Shuffle,
  Swords,
  Trophy,
  UserPlus,
  Users,
  WalletCards
} from "lucide-react";
import { api } from "./api";
import type { AuctionStatus, ChampionshipReport, GamePhase, GameState, Match, Player, President } from "./types";

const MINIMUM_PRESIDENTS = 10;

const phases: Array<{ key: GamePhase; label: string }> = [
  { key: "REGISTRATION", label: "Registration" },
  { key: "DRAFT_AUCTION", label: "Auction" },
  { key: "DRAFT_LOTTERY", label: "Lottery" },
  { key: "CHAMPIONSHIP", label: "Championship" },
  { key: "TRANSFER_WINDOW", label: "Transfers" },
  { key: "FINISHED", label: "Finished" }
];

type Feedback = {
  kind: "success" | "error";
  message: string;
};

type Runner = <T>(action: () => Promise<T>, success?: (result: T) => string) => Promise<boolean>;

function money(value?: number) {
  return new Intl.NumberFormat("en-US", { style: "currency", currency: "BRL" }).format(value ?? 0);
}

function positionLabel(position?: string) {
  const labels: Record<string, string> = {
    GOALKEEPER: "Goalkeeper",
    DEFENDER: "Defender",
    MIDFIELDER: "Midfielder",
    FORWARD: "Forward"
  };
  return labels[position ?? ""] ?? position ?? "-";
}

function phaseIndex(phase?: GamePhase) {
  return phases.findIndex((item) => item.key === phase);
}

function wait(milliseconds: number) {
  return new Promise((resolve) => window.setTimeout(resolve, milliseconds));
}

function useScoutData() {
  const [gameState, setGameState] = useState<GameState | null>(null);
  const [presidents, setPresidents] = useState<President[]>([]);
  const [players, setPlayers] = useState<Player[]>([]);
  const [matches, setMatches] = useState<Match[]>([]);
  const [auction, setAuction] = useState<AuctionStatus | null>(null);
  const [report, setReport] = useState<ChampionshipReport | null>(null);
  const [loading, setLoading] = useState(true);
  const [acting, setActing] = useState(false);
  const [feedback, setFeedback] = useState<Feedback | null>(null);

  async function refresh(showError = true) {
    setLoading(true);
    try {
      const [state, allPresidents, allPlayers] = await Promise.all([
        api.getGameState(),
        api.getPresidents(),
        api.getPlayers()
      ]);
      setGameState(state);
      setPresidents(allPresidents);
      setPlayers(allPlayers);

      const jobs: Promise<void>[] = [
        api.getMatches().then(setMatches).catch(() => setMatches([])),
        api.getFullReport().then(setReport).catch(() => setReport(null))
      ];

      if (state.phase === "DRAFT_AUCTION") {
        jobs.push(api.getCurrentAuction().then(setAuction).catch(() => setAuction(null)));
      } else {
        setAuction(null);
      }

      await Promise.all(jobs);
      return true;
    } catch (caught) {
      if (showError) {
        setFeedback({
          kind: "error",
          message: caught instanceof Error ? caught.message : "Could not load Scout data"
        });
      }
      return false;
    } finally {
      setLoading(false);
    }
  }

  async function run<T>(action: () => Promise<T>, success?: (result: T) => string) {
    setActing(true);
    setFeedback(null);
    try {
      const result = await action();
      const responseMessage =
        typeof result === "object" && result && "message" in result ? String(result.message) : "Action completed";
      setFeedback({ kind: "success", message: success?.(result) ?? responseMessage });
      await refresh(false);
      return true;
    } catch (caught) {
      setFeedback({
        kind: "error",
        message: caught instanceof Error ? caught.message : "Action failed"
      });
      return false;
    } finally {
      setActing(false);
    }
  }

  useEffect(() => {
    let cancelled = false;

    async function loadWhenBackendIsReady() {
      for (let attempt = 0; attempt < 5 && !cancelled; attempt += 1) {
        const loaded = await refresh(attempt === 4);
        if (loaded) return;
        await wait(1000);
      }
    }

    void loadWhenBackendIsReady();
    return () => {
      cancelled = true;
    };
  }, []);

  return {
    acting,
    auction,
    feedback,
    gameState,
    loading,
    matches,
    players,
    presidents,
    refresh,
    report,
    run,
    setFeedback
  };
}

export function App() {
  const data = useScoutData();
  const playedMatches = data.matches.filter((match) => match.played).length;

  return (
    <main className="app-shell">
      <header className="app-header">
        <div className="brand">
          <span className="brand-mark">
            <Shield size={22} />
          </span>
          <div>
            <strong>Scout League</strong>
            <span>Season control</span>
          </div>
        </div>
        <div className="header-actions">
          <button onClick={() => void data.run(api.resetSeason)} disabled={data.acting}>
            <Flag size={17} />
            New season
          </button>
        </div>
      </header>

      <section className="status-strip">
        <div>
          <span>Current phase</span>
          <strong>{phases.find((phase) => phase.key === data.gameState?.phase)?.label ?? "Connecting"}</strong>
        </div>
        <div>
          <span>Clubs</span>
          <strong>{data.presidents.length}</strong>
        </div>
        <div>
          <span>Round</span>
          <strong>
            {data.gameState?.currentRound ?? 0}/{data.gameState?.totalRounds ?? 0}
          </strong>
        </div>
        <div>
          <span>Matches played</span>
          <strong>
            {playedMatches}/{data.matches.length}
          </strong>
        </div>
      </section>

      <PhaseRoadmap phase={data.gameState?.phase} />

      <section className="dashboard">
        <ActivePhasePanel
          acting={data.acting}
          auction={data.auction}
          gameState={data.gameState}
          matches={data.matches}
          players={data.players}
          presidents={data.presidents}
          report={data.report}
          run={data.run}
        />
        <ClubTable phase={data.gameState?.phase} presidents={data.presidents} report={data.report} />
      </section>

      {data.feedback && (
        <NotificationCard feedback={data.feedback} onClose={() => data.setFeedback(null)} />
      )}
    </main>
  );
}

function PhaseRoadmap({ phase }: { phase?: GamePhase }) {
  const activeIndex = phaseIndex(phase);
  return (
    <nav className="phase-roadmap" aria-label="Season phases">
      {phases.map((item, index) => {
        const status = index < activeIndex ? "complete" : index === activeIndex ? "active" : "pending";
        return (
          <div className={`phase-step ${status}`} key={item.key}>
            <span>{index < activeIndex ? <CheckCircle2 size={15} /> : index + 1}</span>
            <strong>{item.label}</strong>
          </div>
        );
      })}
    </nav>
  );
}

function ActivePhasePanel({
  acting,
  auction,
  gameState,
  matches,
  players,
  presidents,
  report,
  run
}: {
  acting: boolean;
  auction: AuctionStatus | null;
  gameState: GameState | null;
  matches: Match[];
  players: Player[];
  presidents: President[];
  report: ChampionshipReport | null;
  run: Runner;
}) {
  return (
    <section className="panel phase-panel">
      <div className="panel-heading">
        <div>
          <p className="eyebrow">Now playing</p>
          <h1>{gameState?.phaseDescription ?? "Connect the backend to begin"}</h1>
        </div>
        <span className="phase-pill">{gameState?.phase?.replaceAll("_", " ") ?? "OFFLINE"}</span>
      </div>

      {gameState?.phase === "REGISTRATION" && (
        <RegistrationPanel acting={acting} presidents={presidents} run={run} />
      )}
      {gameState?.phase === "DRAFT_AUCTION" && (
        <AuctionPanel acting={acting} auction={auction} presidents={presidents} run={run} />
      )}
      {gameState?.phase === "DRAFT_LOTTERY" && (
        <LotteryPanel acting={acting} players={players} presidents={presidents} run={run} />
      )}
      {gameState?.phase === "CHAMPIONSHIP" && (
        <ChampionshipPanel acting={acting} matches={matches} state={gameState} run={run} />
      )}
      {gameState?.phase === "TRANSFER_WINDOW" && (
        <TransfersPanel acting={acting} presidents={presidents} players={players} run={run} />
      )}
      {gameState?.phase === "FINISHED" && <TrophyPanel matches={matches} report={report} />}
    </section>
  );
}

function RegistrationPanel({
  acting,
  presidents,
  run
}: {
  acting: boolean;
  presidents: President[];
  run: Runner;
}) {
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [clubName, setClubName] = useState("");
  const remaining = Math.max(0, MINIMUM_PRESIDENTS - presidents.length);

  return (
    <div className="phase-content">
      <div className="section-heading">
        <div>
          <h2>Register clubs</h2>
          <p>{remaining ? `${remaining} more clubs required to start the auction.` : "Minimum reached. The auction is ready."}</p>
        </div>
        <button
          className="secondary-button"
          disabled={acting}
          onClick={() => void run(api.createDefaultPresidents, () => "The ten default clubs were created.")}
        >
          <Users size={17} />
          Create default clubs
        </button>
      </div>

      <form
        className="entry-form registration-form"
        onSubmit={(event) => {
          event.preventDefault();
          void run(
            () => api.createPresident({ name, email, clubName }),
            (president) => `${president.clubName} was registered.`
          ).then((created) => {
            if (created) {
              setName("");
              setEmail("");
              setClubName("");
            }
          });
        }}
      >
        <label>
          President name
          <input value={name} onChange={(event) => setName(event.target.value)} placeholder="Rafael Menin" required />
        </label>
        <label>
          Email
          <input
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            placeholder="rafael.menin@email.com"
            required
            type="email"
          />
        </label>
        <label>
          Club name
          <input
            value={clubName}
            onChange={(event) => setClubName(event.target.value)}
            placeholder="Atlético Mineiro"
            required
          />
        </label>
        <button type="submit" disabled={acting}>
          <UserPlus size={17} />
          Add club
        </button>
      </form>

      <div className="table-card">
        <div className="table-title">
          <strong>Registered clubs</strong>
          <span>{presidents.length}/{MINIMUM_PRESIDENTS} minimum</span>
        </div>
        <div className="data-table registration-table">
          <div className="table-row table-head">
            <span>#</span>
            <span>Club</span>
            <span>President</span>
            <span>Email</span>
          </div>
          {presidents.map((president, index) => (
            <div className="table-row" key={president.id}>
              <span>{index + 1}</span>
              <strong>{president.clubName || "Unnamed club"}</strong>
              <span>{president.name}</span>
              <span className="truncate">{president.email}</span>
            </div>
          ))}
          {!presidents.length && <p className="empty-state">No clubs registered yet.</p>}
        </div>
      </div>

      <div className="phase-actions">
        <button
          disabled={acting || presidents.length < MINIMUM_PRESIDENTS}
          onClick={() => void run(api.startAuction)}
        >
          <BadgeDollarSign size={17} />
          Start auction
        </button>
      </div>
    </div>
  );
}

function AuctionPanel({
  acting,
  auction,
  presidents,
  run
}: {
  acting: boolean;
  auction: AuctionStatus | null;
  presidents: President[];
  run: Runner;
}) {
  const [presidentId, setPresidentId] = useState("");
  const [bidAmount, setBidAmount] = useState("");

  if (!auction) {
    return <p className="empty-state">Loading the current auction player.</p>;
  }

  return (
    <div className="phase-content">
      <div className="auction-focus">
        <div>
          <span>{positionLabel(auction.playerPosition)}</span>
          <h2>{auction.playerName}</h2>
          <p>Base value {money(auction.playerBaseValue)}</p>
        </div>
        <div className="leading-bid">
          <span>Highest bid</span>
          <strong>{auction.currentLeader ? money(auction.currentHighestBid) : "No bids"}</strong>
          <small>{auction.currentLeader ?? "Waiting for the first offer"}</small>
        </div>
      </div>

      <form
        className="entry-form bid-form"
        onSubmit={(event) => {
          event.preventDefault();
          void run(() =>
            api.placeBid(auction.playerId, {
              presidentId: Number(presidentId),
              bidAmount: Number(bidAmount)
            })
          ).then((placed) => {
            if (placed) setBidAmount("");
          });
        }}
      >
        <label>
          Club
          <select value={presidentId} onChange={(event) => setPresidentId(event.target.value)} required>
            <option value="">Choose a club</option>
            {presidents.map((president) => (
              <option key={president.id} value={president.id}>
                {president.clubName} - {president.name}
              </option>
            ))}
          </select>
        </label>
        <label>
          Bid amount
          <input
            min={auction.playerBaseValue}
            step="0.1"
            type="number"
            value={bidAmount}
            onChange={(event) => setBidAmount(event.target.value)}
            placeholder={String(auction.playerBaseValue)}
            required
          />
        </label>
        <button type="submit" disabled={acting}>
          <CircleDollarSign size={17} />
          Place bid
        </button>
      </form>

      {!!auction.bids.length && (
        <div className="bid-history">
          <strong>Latest bids</strong>
          {auction.bids.slice(0, 4).map((bid, index) => (
            <div key={`${bid.presidentName}-${index}`}>
              <span>{bid.presidentName}</span>
              <b>{money(bid.bidAmount)}</b>
            </div>
          ))}
        </div>
      )}

      <div className="phase-actions">
        <button
          disabled={acting}
          onClick={() =>
            void run(() => api.finalizeAuction(auction.playerId), () => `${auction.playerName}'s auction was finalized.`)
          }
        >
          <CheckCircle2 size={17} />
          Finalize player
        </button>
      </div>
    </div>
  );
}

function LotteryPanel({
  acting,
  players,
  presidents,
  run
}: {
  acting: boolean;
  players: Player[];
  presidents: President[];
  run: Runner;
}) {
  const availablePlayers = players.filter((player) => player.available).length;
  const openSlots = presidents.reduce((total, president) => total + Math.max(0, 5 - president.team.length), 0);

  return (
    <div className="phase-content centered-content">
      <div className="lottery-card">
        <Shuffle size={34} />
        <h2>Complete every squad automatically</h2>
        <p>
          {availablePlayers} available players will fill {openSlots} open squad slots, including one goalkeeper per club.
        </p>
      </div>
      <button
        disabled={acting}
        onClick={() =>
          void run(api.runLottery, () => "Lottery completed. The championship schedule was generated automatically.")
        }
      >
        <Shuffle size={17} />
        Run lottery
      </button>
    </div>
  );
}

function ChampionshipPanel({
  acting,
  matches,
  state,
  run
}: {
  acting: boolean;
  matches: Match[];
  state: GameState;
  run: Runner;
}) {
  const firstVisibleRound = state.currentRound > 0 ? state.currentRound : 1;
  const [visibleRound, setVisibleRound] = useState(firstVisibleRound);
  const roundMatches = matches.filter((match) => match.roundNumber === visibleRound);
  const roundPlayed = roundMatches.length > 0 && roundMatches.every((match) => match.played);
  const nextRound = Math.min(visibleRound + 1, state.totalRounds);
  const progress = state.totalRounds ? Math.round((state.currentRound / state.totalRounds) * 100) : 0;
  const transfersLocked = state.currentRound < 3;

  useEffect(() => {
    setVisibleRound(state.currentRound > 0 ? state.currentRound : 1);
  }, [state.currentRound]);

  return (
    <div className="phase-content">
      <div className="round-header">
        <div>
          <span>{roundPlayed ? "Round results" : "Next round"}</span>
          <h2>Round {visibleRound}</h2>
        </div>
        <div className="round-progress">
          <span>{progress}% complete</span>
          <div>
            <i style={{ width: `${progress}%` }} />
          </div>
        </div>
      </div>

      <div className="fixtures">
        {roundMatches.map((match) => (
          <MatchRow key={match.id} match={match} />
        ))}
        {!roundMatches.length && <p className="empty-state">The schedule is being prepared.</p>}
      </div>

      <div className="phase-actions">
        {roundPlayed ? (
          <button
            disabled={acting || visibleRound >= state.totalRounds}
            onClick={() => setVisibleRound(nextRound)}
          >
            <ChevronsRight size={17} />
            Next round
          </button>
        ) : (
          <button
            disabled={acting || !roundMatches.length}
            onClick={() => void run(() => api.simulateRound(visibleRound))}
          >
            <Swords size={17} />
            Play round {visibleRound}
          </button>
        )}
        <button
          className="secondary-button"
          disabled={acting || !matches.some((match) => !match.played)}
          onClick={() => void run(api.simulateAll, () => "All remaining championship matches were played.")}
        >
          <FastForward size={17} />
          Play all matches
        </button>
        <button
          className="secondary-button"
          disabled={acting || transfersLocked}
          title={transfersLocked ? "Transfers unlock after round 3" : "Open the transfer window"}
          onClick={() => void run(api.openTransferWindow)}
        >
          <Shuffle size={17} />
          Open transfers
        </button>
      </div>
      <p className="helper-text">
        Review each result before moving to the next round, or play every remaining match at once. Transfers unlock
        after round 3.
      </p>
    </div>
  );
}

function TransfersPanel({
  acting,
  presidents,
  players,
  run
}: {
  acting: boolean;
  presidents: President[];
  players: Player[];
  run: Runner;
}) {
  const [presidentId, setPresidentId] = useState("");
  const [playerOutId, setPlayerOutId] = useState("");
  const [playerInId, setPlayerInId] = useState("");
  const marketPlayers = players.filter((player) => player.available);
  const selectedPresident = presidents.find((president) => president.id === Number(presidentId));

  return (
    <div className="phase-content">
      <form
        className="entry-form transfer-form"
        onSubmit={(event) => {
          event.preventDefault();
          void run(() =>
            api.swapWithMarket({
              presidentId: Number(presidentId),
              playerOutId: Number(playerOutId),
              playerInId: Number(playerInId)
            })
          );
        }}
      >
        <label>
          Club
          <select
            value={presidentId}
            onChange={(event) => {
              setPresidentId(event.target.value);
              setPlayerOutId("");
            }}
            required
          >
            <option value="">Choose a club</option>
            {presidents.map((president) => (
              <option key={president.id} value={president.id}>
                {president.clubName}
              </option>
            ))}
          </select>
        </label>
        <label>
          Player out
          <select value={playerOutId} onChange={(event) => setPlayerOutId(event.target.value)} required>
            <option value="">Choose a player</option>
            {selectedPresident?.team.map((player) => (
              <option key={player.id} value={player.id}>
                {player.name} - {positionLabel(player.position)}
              </option>
            ))}
          </select>
        </label>
        <label>
          Player in
          <select value={playerInId} onChange={(event) => setPlayerInId(event.target.value)} required>
            <option value="">Choose from market</option>
            {marketPlayers.map((player) => (
              <option key={player.id} value={player.id}>
                {player.name} - {money(player.value)}
              </option>
            ))}
          </select>
        </label>
        <button type="submit" disabled={acting}>
          <Shuffle size={17} />
          Confirm transfer
        </button>
      </form>

      <div className="phase-actions">
        <button className="secondary-button" disabled={acting} onClick={() => void run(api.closeTransferWindow)}>
          <CheckCircle2 size={17} />
          Close transfer window
        </button>
      </div>
    </div>
  );
}

function TrophyPanel({ matches, report }: { matches: Match[]; report: ChampionshipReport | null }) {
  const rounds = useMemo(
    () =>
      Array.from(new Set(matches.map((match) => match.roundNumber)))
        .sort((a, b) => a - b)
        .map((roundNumber) => ({
          roundNumber,
          matches: matches.filter((match) => match.roundNumber === roundNumber)
        })),
    [matches]
  );

  return (
    <div className="season-summary">
      <div className="trophy-card">
        <Trophy size={46} />
        <span>Season champion</span>
        <h2>{report?.champion ?? "Final table pending"}</h2>
        <p>The season is complete. Every score and the final club statistics are listed below.</p>
      </div>

      <div className="final-standings">
        <div className="summary-heading">
          <div>
            <p className="eyebrow">Final table</p>
            <h2>Goals summary</h2>
          </div>
        </div>
        <div className="final-table">
          <div className="final-table-row final-table-head">
            <span>Pos</span>
            <span>Club</span>
            <span>GF</span>
            <span>GA</span>
            <span>GD</span>
          </div>
          {(report?.standings ?? []).map((standing) => (
            <div className="final-table-row" key={standing.presidentName}>
              <b>{standing.position}</b>
              <strong>{standing.clubName || standing.presidentName}</strong>
              <span>{standing.goalsFor}</span>
              <span>{standing.goalsAgainst}</span>
              <span>{standing.goalDifference > 0 ? `+${standing.goalDifference}` : standing.goalDifference}</span>
            </div>
          ))}
        </div>
      </div>

      <div className="round-results">
        <div className="summary-heading">
          <div>
            <p className="eyebrow">All matches</p>
            <h2>Results by round</h2>
          </div>
          <span>{matches.length} matches</span>
        </div>
        {rounds.map((round, index) => (
          <details className="round-result-card" key={round.roundNumber} open={index === rounds.length - 1}>
            <summary>
              <strong>Round {round.roundNumber}</strong>
              <span>{round.matches.length} matches</span>
            </summary>
            <div className="round-result-list">
              {round.matches.map((match) => (
                <MatchRow key={match.id} match={match} />
              ))}
            </div>
          </details>
        ))}
      </div>
    </div>
  );
}

function ClubTable({
  phase,
  presidents,
  report
}: {
  phase?: GamePhase;
  presidents: President[];
  report: ChampionshipReport | null;
}) {
  const rows = useMemo(() => {
    if (report?.standings?.length) {
      return report.standings.map((standing) => ({
        ...standing,
        president: presidents.find((item) => item.name === standing.presidentName)
      }));
    }
    return presidents.map((president, index) => ({
      position: index + 1,
      presidentName: president.name,
      clubName: president.clubName,
      points: president.points,
      wins: president.wins,
      draws: president.draws,
      losses: president.losses,
      goalsFor: president.goalsFor,
      goalsAgainst: president.goalsAgainst,
      goalDifference: president.goalDifference,
      matchesPlayed: president.wins + president.draws + president.losses,
      president
    }));
  }, [presidents, report]);

  const showBudget = phase === "DRAFT_AUCTION";

  return (
    <aside className="panel clubs-panel">
      <div className="panel-heading compact">
        <div>
          <p className="eyebrow">League table</p>
          <h2>Club positions</h2>
        </div>
        {showBudget ? <WalletCards size={21} /> : <Trophy size={21} />}
      </div>

      <div className="club-table">
        <div className={`club-row club-head ${showBudget ? "with-budget" : ""}`}>
          <span>Pos</span>
          <span>Club</span>
          <span>{showBudget ? "Budget" : "Pts"}</span>
        </div>
        {rows.map((row) => (
          <div className={`club-row ${showBudget ? "with-budget" : ""}`} key={row.presidentName}>
            <span className="position-number">{row.position}</span>
            <div>
              <strong>{row.clubName || row.presidentName}</strong>
              <small>{row.presidentName}</small>
            </div>
            <b>{showBudget ? money(row.president?.budget) : row.points}</b>
          </div>
        ))}
        {!rows.length && <p className="empty-state">Club positions will appear here.</p>}
      </div>
    </aside>
  );
}

function MatchRow({ match }: { match: Match }) {
  return (
    <article className="fixture-row">
      <strong>{match.homeClub || match.homePresident}</strong>
      <b>{match.played ? `${match.homeGoals} - ${match.awayGoals}` : "vs"}</b>
      <strong>{match.awayClub || match.awayPresident}</strong>
    </article>
  );
}

function NotificationCard({ feedback, onClose }: { feedback: Feedback; onClose: () => void }) {
  return (
    <div className="notification-backdrop" role="dialog" aria-modal="true" aria-live="assertive">
      <div className={`notification-card ${feedback.kind}`}>
        <span className="notification-icon">
          {feedback.kind === "success" ? <CheckCircle2 size={26} /> : <CircleAlert size={26} />}
        </span>
        <div>
          <p className="eyebrow">{feedback.kind === "success" ? "Success" : "Action needed"}</p>
          <h2>{feedback.kind === "success" ? "Done" : "Something went wrong"}</h2>
          <p>{feedback.message}</p>
        </div>
        <button onClick={onClose}>OK</button>
      </div>
    </div>
  );
}
