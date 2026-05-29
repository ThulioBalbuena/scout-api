import { useEffect, useMemo, useState, type ReactNode } from "react";
import {
  BadgeDollarSign,
  CalendarDays,
  CheckCircle2,
  CircleDollarSign,
  Flag,
  Loader2,
  Play,
  RefreshCw,
  Shield,
  Shuffle,
  Swords,
  Trophy,
  UserPlus,
  Users
} from "lucide-react";
import { api } from "./api";
import type { AuctionStatus, ChampionshipReport, GameState, GamePhase, Match, Player, President } from "./types";

const phases: Array<{
  key: GamePhase;
  title: string;
  label: string;
  action: string;
}> = [
  { key: "REGISTRATION", title: "Sign presidents", label: "Registration", action: "Create clubs" },
  { key: "DRAFT_AUCTION", title: "Auction stars", label: "Auction", action: "Bid and finalize" },
  { key: "DRAFT_LOTTERY", title: "Lottery draft", label: "Lottery", action: "Run lottery" },
  { key: "CHAMPIONSHIP", title: "Play league", label: "Championship", action: "Schedule and simulate" },
  { key: "TRANSFER_WINDOW", title: "Transfer window", label: "Transfers", action: "Swap players" },
  { key: "FINISHED", title: "Lift trophy", label: "Reports", action: "Review champion" }
];

function money(value?: number) {
  return new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(value ?? 0);
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

function useScoutData() {
  const [gameState, setGameState] = useState<GameState | null>(null);
  const [presidents, setPresidents] = useState<President[]>([]);
  const [players, setPlayers] = useState<Player[]>([]);
  const [matches, setMatches] = useState<Match[]>([]);
  const [auction, setAuction] = useState<AuctionStatus | null>(null);
  const [report, setReport] = useState<ChampionshipReport | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);

  async function refresh() {
    setLoading(true);
    setError(null);
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
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : "Could not load Scout data");
    } finally {
      setLoading(false);
    }
  }

  async function run<T>(action: () => Promise<T>, message?: (result: T) => string) {
    setError(null);
    setNotice(null);
    try {
      const result = await action();
      const responseMessage = typeof result === "object" && result && "message" in result ? String(result.message) : null;
      setNotice(message?.(result) ?? responseMessage ?? "Action completed");
      await refresh();
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : "Action failed");
    }
  }

  useEffect(() => {
    void refresh();
  }, []);

  return { auction, error, gameState, loading, matches, notice, players, presidents, refresh, report, run };
}

export function App() {
  const data = useScoutData();
  const activeIndex = phaseIndex(data.gameState?.phase);
  const playedMatches = data.matches.filter((match) => match.played).length;

  return (
    <main>
      <section className="hero">
        <div className="hero-copy">
          <p className="eyebrow">Scout Fantasy Football</p>
          <h1>Run the season from kickoff to trophy.</h1>
          <p className="hero-text">
            Follow the game in order: register presidents, auction the stars, draft the squads, simulate the league,
            open transfers, and crown the champion.
          </p>
          <div className="hero-stats">
            <Stat label="Presidents" value={data.presidents.length} />
            <Stat label="Players" value={data.players.length} />
            <Stat label="Played" value={`${playedMatches}/${data.matches.length}`} />
          </div>
        </div>
        <div className="pitch-card" aria-hidden="true">
          <div className="pitch-lines">
            <span className="center-circle" />
            <span className="spot home" />
            <span className="spot away" />
          </div>
          <div className="scoreboard">
            <span>Current phase</span>
            <strong>{data.gameState?.phase ?? "Loading"}</strong>
          </div>
        </div>
      </section>

      <section className="page-wrap">
        <div className="toolbar">
          <div>
            <p className="section-kicker">Match flow</p>
            <h2>Execute each phase in order</h2>
          </div>
          <div className="toolbar-actions">
            <button className="reset-button" onClick={() => data.run(api.resetSeason)} title="Start a fresh season">
              <Flag size={17} />
              New season
            </button>
            <button className="ghost-button" onClick={data.refresh} title="Refresh data">
              {data.loading ? <Loader2 className="spin" size={17} /> : <RefreshCw size={17} />}
              Refresh
            </button>
          </div>
        </div>

        {data.error && <div className="alert error">{data.error}</div>}
        {data.notice && <div className="alert success">{data.notice}</div>}

        <PhaseRoadmap activeIndex={activeIndex} />

        <section className="matchday-grid">
          <ActivePhasePanel {...data} />
          <SquadsPanel presidents={data.presidents} players={data.players} />
          <LeaguePanel matches={data.matches} report={data.report} />
        </section>
      </section>
    </main>
  );
}

function Stat({ label, value }: { label: string; value: string | number }) {
  return (
    <div className="hero-stat">
      <strong>{value}</strong>
      <span>{label}</span>
    </div>
  );
}

function PhaseRoadmap({ activeIndex }: { activeIndex: number }) {
  return (
    <div className="roadmap">
      {phases.map((phase, index) => {
        const status = index < activeIndex ? "complete" : index === activeIndex ? "active" : "locked";
        return (
          <article className={`phase-card ${status}`} key={phase.key}>
            <span className="phase-number">{index + 1}</span>
            <div>
              <strong>{phase.label}</strong>
              <p>{phase.action}</p>
            </div>
          </article>
        );
      })}
    </div>
  );
}

function ActivePhasePanel({
  auction,
  gameState,
  matches,
  players,
  presidents,
  report,
  run
}: {
  auction: AuctionStatus | null;
  gameState: GameState | null;
  matches: Match[];
  players: Player[];
  presidents: President[];
  report: ChampionshipReport | null;
  run: <T>(action: () => Promise<T>, message?: (result: T) => string) => Promise<void>;
}) {
  return (
    <section className="panel active-panel">
      <div className="panel-heading">
        <div>
          <p className="section-kicker">Now playing</p>
          <h2>{phases[phaseIndex(gameState?.phase)]?.title ?? "Connect backend"}</h2>
        </div>
        <span className="phase-pill">{gameState?.phase ?? "Offline"}</span>
      </div>

      <div className="phase-note">
        <Flag size={18} />
        <p>{gameState?.phaseDescription ?? "Start the Spring API on port 8080 and refresh this page."}</p>
      </div>

      {gameState?.phase === "REGISTRATION" && <RegistrationPanel presidents={presidents} run={run} />}
      {gameState?.phase === "DRAFT_AUCTION" && <AuctionPanel auction={auction} presidents={presidents} run={run} />}
      {gameState?.phase === "DRAFT_LOTTERY" && <LotteryPanel players={players} run={run} />}
      {gameState?.phase === "CHAMPIONSHIP" && <ChampionshipPanel matches={matches} state={gameState} run={run} />}
      {gameState?.phase === "TRANSFER_WINDOW" && <TransfersPanel presidents={presidents} players={players} run={run} />}
      {gameState?.phase === "FINISHED" && <TrophyPanel report={report} />}

      <NextActions phase={gameState?.phase} auction={auction} run={run} />
    </section>
  );
}

function NextActions({
  auction,
  phase,
  run
}: {
  auction: AuctionStatus | null;
  phase?: GamePhase;
  run: <T>(action: () => Promise<T>, message?: (result: T) => string) => Promise<void>;
}) {
  const actions: Partial<Record<GamePhase, Array<{ label: string; icon: ReactNode; onClick: () => Promise<unknown> }>>> = {
    REGISTRATION: [{ label: "Start auction", icon: <BadgeDollarSign size={17} />, onClick: api.startAuction }],
    DRAFT_AUCTION: [
      ...(auction
        ? [{ label: "Finalize player", icon: <CheckCircle2 size={17} />, onClick: () => api.finalizeAuction(auction.playerId) }]
        : []),
      { label: "Next auction", icon: <Play size={17} />, onClick: api.advanceAuction }
    ],
    DRAFT_LOTTERY: [{ label: "Run lottery", icon: <Shuffle size={17} />, onClick: api.runLottery }],
    CHAMPIONSHIP: [
      { label: "Generate schedule", icon: <CalendarDays size={17} />, onClick: api.generateSchedule },
      { label: "Simulate all", icon: <Swords size={17} />, onClick: api.simulateAll },
      { label: "Open transfers", icon: <Shuffle size={17} />, onClick: api.openTransferWindow },
      { label: "Finish season", icon: <Flag size={17} />, onClick: api.finishChampionship }
    ],
    TRANSFER_WINDOW: [{ label: "Close transfer window", icon: <CheckCircle2 size={17} />, onClick: api.closeTransferWindow }],
    FINISHED: [{ label: "Start new season", icon: <Flag size={17} />, onClick: api.resetSeason }]
  };

  const visibleActions = phase ? actions[phase] ?? [] : [];
  if (!visibleActions.length) return null;

  return (
    <div className="next-actions">
      {visibleActions.map((action) => (
        <button key={action.label} onClick={() => run(action.onClick)}>
          {action.icon}
          {action.label}
        </button>
      ))}
    </div>
  );
}

function RegistrationPanel({
  presidents,
  run
}: {
  presidents: President[];
  run: <T>(action: () => Promise<T>, message?: (result: T) => string) => Promise<void>;
}) {
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");

  return (
    <div className="phase-workbench">
      <form
        className="manager-form"
        onSubmit={(event) => {
          event.preventDefault();
          void run(() => api.createPresident({ name, email }), (president) => `Created ${president.name}`).then(() => {
            setName("");
            setEmail("");
          });
        }}
      >
        <label>
          President name
          <input value={name} onChange={(event) => setName(event.target.value)} placeholder="Ex: Maria Santos" required />
        </label>
        <label>
          Email
          <input
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            placeholder="maria@email.com"
            required
            type="email"
          />
        </label>
        <button type="submit">
          <UserPlus size={17} />
          Register president
        </button>
      </form>
      <MiniList title="Registered clubs" empty="No presidents registered yet.">
        {presidents.map((president) => (
          <Row key={president.id} title={president.name} meta={`${president.team?.length ?? 0}/5 players`} value={money(president.budget)} />
        ))}
      </MiniList>
    </div>
  );
}

function AuctionPanel({
  auction,
  presidents,
  run
}: {
  auction: AuctionStatus | null;
  presidents: President[];
  run: <T>(action: () => Promise<T>, message?: (result: T) => string) => Promise<void>;
}) {
  const [presidentId, setPresidentId] = useState("");
  const [bidAmount, setBidAmount] = useState("");

  if (!auction) {
    return <p className="empty">No current auction player. Use the ordered action below to move the auction forward.</p>;
  }

  return (
    <div className="phase-workbench">
      <div className="star-card">
        <span>{positionLabel(auction.playerPosition)}</span>
        <strong>{auction.playerName}</strong>
        <p>Base value {money(auction.playerBaseValue)}</p>
        <b>{auction.currentLeader ? `${auction.currentLeader} leads with ${money(auction.currentHighestBid)}` : "No bids yet"}</b>
      </div>
      <form
        className="manager-form compact-form"
        onSubmit={(event) => {
          event.preventDefault();
          void run(() => api.placeBid(auction.playerId, { presidentId: Number(presidentId), bidAmount: Number(bidAmount) }));
        }}
      >
        <label>
          President
          <select value={presidentId} onChange={(event) => setPresidentId(event.target.value)} required>
            <option value="">Choose president</option>
            {presidents.map((president) => (
              <option key={president.id} value={president.id}>
                {president.name}
              </option>
            ))}
          </select>
        </label>
        <label>
          Bid amount
          <input
            min="0.1"
            step="0.1"
            type="number"
            value={bidAmount}
            onChange={(event) => setBidAmount(event.target.value)}
            placeholder="120"
            required
          />
        </label>
        <button type="submit">
          <CircleDollarSign size={17} />
          Place bid
        </button>
      </form>
      <MiniList title="Bid history" empty="No bids placed.">
        {auction.bids.map((bid, index) => (
          <Row key={`${bid.presidentName}-${index}`} title={bid.presidentName} meta={bid.bidTime} value={money(bid.bidAmount)} />
        ))}
      </MiniList>
    </div>
  );
}

function LotteryPanel({
  players,
  run
}: {
  players: Player[];
  run: <T>(action: () => Promise<T>, message?: (result: T) => string) => Promise<void>;
}) {
  const availablePlayers = players.filter((player) => player.available).slice(0, 10);
  return (
    <div className="phase-workbench">
      <div className="lottery-ball">
        <Shuffle size={36} />
        <strong>{availablePlayers.length}</strong>
        <span>players waiting</span>
      </div>
      <button className="primary-wide" onClick={() => run(api.runLottery)}>
        <Shuffle size={17} />
        Run automatic lottery
      </button>
      <MiniList title="Lottery pool" empty="No available players listed.">
        {availablePlayers.map((player) => (
          <Row key={player.id} title={player.name} meta={positionLabel(player.position)} value={money(player.value)} />
        ))}
      </MiniList>
    </div>
  );
}

function ChampionshipPanel({
  matches,
  state,
  run
}: {
  matches: Match[];
  state: GameState | null;
  run: <T>(action: () => Promise<T>, message?: (result: T) => string) => Promise<void>;
}) {
  const [round, setRound] = useState("1");
  const visibleMatches = matches.slice(0, 6);

  return (
    <div className="phase-workbench">
      <form
        className="manager-form compact-form"
        onSubmit={(event) => {
          event.preventDefault();
          void run(() => api.simulateRound(Number(round)));
        }}
      >
        <label>
          Round
          <input min="1" max={state?.totalRounds ?? 10} type="number" value={round} onChange={(event) => setRound(event.target.value)} />
        </label>
        <button type="submit">
          <Swords size={17} />
          Simulate round
        </button>
      </form>
      <MiniList title="Fixture board" empty="Generate the schedule to see matches.">
        {visibleMatches.map((match) => (
          <MatchRow key={match.id} match={match} />
        ))}
      </MiniList>
    </div>
  );
}

function TransfersPanel({
  presidents,
  players,
  run
}: {
  presidents: President[];
  players: Player[];
  run: <T>(action: () => Promise<T>, message?: (result: T) => string) => Promise<void>;
}) {
  const [presidentId, setPresidentId] = useState("");
  const [playerOutId, setPlayerOutId] = useState("");
  const [playerInId, setPlayerInId] = useState("");
  const marketPlayers = players.filter((player) => player.available);
  const selectedPresident = presidents.find((president) => president.id === Number(presidentId));

  return (
    <form
      className="manager-form transfer-form"
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
        President
        <select value={presidentId} onChange={(event) => setPresidentId(event.target.value)} required>
          <option value="">Choose president</option>
          {presidents.map((president) => (
            <option key={president.id} value={president.id}>
              {president.name}
            </option>
          ))}
        </select>
      </label>
      <label>
        Player out
        <select value={playerOutId} onChange={(event) => setPlayerOutId(event.target.value)} required>
          <option value="">Choose player</option>
          {selectedPresident?.team?.map((player) => (
            <option key={player.id} value={player.id}>
              {player.name}
            </option>
          ))}
        </select>
      </label>
      <label>
        Player in
        <select value={playerInId} onChange={(event) => setPlayerInId(event.target.value)} required>
          <option value="">Market player</option>
          {marketPlayers.map((player) => (
            <option key={player.id} value={player.id}>
              {player.name}
            </option>
          ))}
        </select>
      </label>
      <button type="submit">
        <Shuffle size={17} />
        Swap with market
      </button>
    </form>
  );
}

function TrophyPanel({ report }: { report: ChampionshipReport | null }) {
  return (
    <div className="trophy-panel">
      <Trophy size={42} />
      <span>Champion</span>
      <strong>{report?.champion ?? "Waiting for final whistle"}</strong>
    </div>
  );
}

function SquadsPanel({ presidents, players }: { presidents: President[]; players: Player[] }) {
  const featuredPlayers = useMemo(() => players.slice(0, 10), [players]);

  return (
    <section className="panel side-panel">
      <div className="panel-heading small">
        <div>
          <p className="section-kicker">Clubs</p>
          <h2>Squads and market</h2>
        </div>
        <Users size={20} />
      </div>
      <MiniList title="Presidents" empty="No clubs yet.">
        {presidents.slice(0, 6).map((president) => (
          <Row
            key={president.id}
            title={president.name}
            meta={`${president.wins}-${president.draws}-${president.losses}`}
            value={`${president.points} pts`}
          />
        ))}
      </MiniList>
      <MiniList title="Player catalog" empty="No players loaded.">
        {featuredPlayers.map((player) => (
          <Row key={player.id} title={player.name} meta={positionLabel(player.position)} value={player.presidentName ?? "Market"} />
        ))}
      </MiniList>
    </section>
  );
}

function LeaguePanel({ matches, report }: { matches: Match[]; report: ChampionshipReport | null }) {
  return (
    <section className="panel side-panel">
      <div className="panel-heading small">
        <div>
          <p className="section-kicker">League table</p>
          <h2>Results and reports</h2>
        </div>
        <Trophy size={20} />
      </div>
      <div className="standings">
        {(report?.standings ?? []).slice(0, 6).map((standing) => (
          <div className="standing-row" key={standing.presidentName}>
            <span>{standing.position}</span>
            <strong>{standing.presidentName}</strong>
            <b>{standing.points}</b>
          </div>
        ))}
        {!report?.standings?.length && <p className="empty">Standings appear after championship matches.</p>}
      </div>
      <MiniList title="Recent matches" empty="No matches generated.">
        {matches.slice(0, 5).map((match) => (
          <MatchRow key={match.id} match={match} />
        ))}
      </MiniList>
    </section>
  );
}

function MiniList({ children, empty, title }: { children: ReactNode; empty: string; title: string }) {
  const hasItems = Array.isArray(children) ? children.length > 0 : Boolean(children);
  return (
    <div className="mini-list">
      <h3>{title}</h3>
      {hasItems ? children : <p className="empty">{empty}</p>}
    </div>
  );
}

function Row({ meta, title, value }: { meta: string; title: string; value: string }) {
  return (
    <article className="data-row">
      <div>
        <strong>{title}</strong>
        <span>{meta}</span>
      </div>
      <b>{value}</b>
    </article>
  );
}

function MatchRow({ match }: { match: Match }) {
  return (
    <article className="match-row">
      <span>R{match.roundNumber}</span>
      <strong>{match.homePresident}</strong>
      <b>{match.played ? `${match.homeGoals} - ${match.awayGoals}` : "vs"}</b>
      <strong>{match.awayPresident}</strong>
    </article>
  );
}
