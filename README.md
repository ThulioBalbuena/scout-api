# Scout

Scout é uma aplicação full stack para um fantasy game de futebol, inspirado em competições no estilo Cartola. O sistema controla o ciclo do jogo: cadastro de presidentes, draft de jogadores, simulação do campeonato, transferências e relatórios finais.

## Tecnologias

- Java 21
- Spring Boot 4
- PostgreSQL 16
- Swagger UI
- Maven Wrapper
- Lombok
- React 19
- TypeScript
- Vite
- Nginx
- Docker Compose

## Estrutura do Projeto

```text
scout-api/
├── Scout5/          # API REST em Spring Boot
├── Scout-front/     # Frontend em React + Vite
├── docker-compose.yml
└── README.md
```

No backend, os principais pacotes ficam em `Scout5/src/main/java/com/balbuena/Scout`:

- `controller`: endpoints REST.
- `service`: regras de negócio e fluxo do jogo.
- `model`: entidades JPA e enums.
- `repository`: acesso ao banco de dados com Spring Data JPA.
- `dto`: objetos de requisição e resposta.
- `config`: configurações da aplicação e dados iniciais.
- `exception`: tratamento de erros da API.

## Fluxo do Jogo

1. `REGISTRATION`: presidentes são cadastrados e recebem um orçamento inicial.
2. `DRAFT_AUCTION`: jogadores especiais entram em leilão.
3. `DRAFT_LOTTERY`: jogadores restantes são distribuídos automaticamente para completar os times.
4. `CHAMPIONSHIP`: a tabela é gerada e as partidas podem ser simuladas por rodada ou todas de uma vez.
5. `TRANSFER_WINDOW`: presidentes podem trocar jogadores com o mercado ou negociar entre si.
6. `FINISHED`: o campeonato é encerrado e os relatórios finais ficam disponíveis.

## Como Rodar com Docker

Crie um arquivo `.env` na raiz do projeto, ao lado do `docker-compose.yml`:

```env
POSTGRES_DB=scout
POSTGRES_USER=scout
POSTGRES_PASSWORD=scout
POSTGRES_PORT=5432
BACKEND_PORT=8080
FRONTEND_PORT=5173
```

Suba a aplicação:

```bash
docker compose up --build
```

Serviços disponíveis:

- Frontend: `http://localhost:5173`
- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

Para parar os containers:

```bash
docker compose down
```

Para parar e remover o volume do PostgreSQL:

```bash
docker compose down -v
```

## Rodando em Desenvolvimento

### Banco de dados

Com o `.env` criado na raiz do projeto, suba apenas o PostgreSQL:

```bash
docker compose up -d postgres
```

### Backend

```bash
cd Scout5
./mvnw spring-boot:run
```

No Windows PowerShell:

```powershell
cd Scout5
.\mvnw.cmd spring-boot:run
```

A API usa as variáveis do `.env` da raiz do projeto ou de um `.env` dentro de `Scout5`.

### Frontend

Em outro terminal:

```bash
cd Scout-front
npm install
npm run dev
```

O Vite roda em `http://localhost:5173` e redireciona chamadas para `/api` ao backend em `http://localhost:8080`.

Se quiser apontar o frontend para outro backend, crie `Scout-front/.env.local`:

```env
VITE_API_BASE_URL=http://localhost:8080
```

## Principais Rotas da API

- `GET /api/game/state`: estado atual do jogo.
- `POST /api/game/start-auction`: inicia o draft por leilão.
- `POST /api/game/advance-auction`: avança para o próximo jogador do leilão.
- `POST /api/game/advance-to-championship`: avança para o campeonato.
- `POST /api/game/open-transfer-window`: abre janela de transferências.
- `POST /api/game/close-transfer-window`: fecha janela de transferências.
- `POST /api/game/finish`: finaliza o campeonato.
- `POST /api/game/reset`: reinicia o jogo.
- `GET /api/presidents`: lista presidentes.
- `POST /api/presidents`: cadastra presidente.
- `GET /api/players`: lista jogadores.
- `GET /api/players/available`: lista jogadores disponíveis.
- `GET /api/players/auction`: lista jogadores de leilão.
- `GET /api/auction/current`: consulta o leilão atual.
- `POST /api/auction/players/{playerId}/bid`: registra lance.
- `POST /api/auction/players/{playerId}/finalize`: finaliza leilão do jogador.
- `GET /api/lottery/available`: lista jogadores disponíveis para sorteio.
- `POST /api/lottery/run`: executa o sorteio.
- `POST /api/championship/generate-schedule`: gera a tabela.
- `GET /api/championship/matches`: lista partidas.
- `POST /api/championship/simulate/all`: simula todas as partidas.
- `GET /api/transfers/available`: lista jogadores disponíveis para transferência.
- `POST /api/transfers/swap`: troca jogador com o mercado.
- `POST /api/transfers/negotiate`: negocia jogadores entre presidentes.
- `GET /api/reports/full-report`: relatório completo.

## Testes e Build

Backend:

```bash
cd Scout5
./mvnw test
./mvnw clean package
```

Frontend:

```bash
cd Scout-front
npm run build
```
