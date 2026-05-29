# Scout Frontend

React + TypeScript frontend for the Scout fantasy football API.

## Requirements

- Node.js 20+
- Scout backend running at `http://localhost:8080`

## Setup

```bash
npm install
npm run dev
```

The Vite dev server runs on `http://localhost:5173` and proxies `/api` requests to the Spring Boot backend.

To point the app at another backend without the dev proxy, create `.env.local`:

```env
VITE_API_BASE_URL=http://localhost:8080
```

## Scripts

- `npm run dev`: start the development server.
- `npm run build`: type-check and build production assets.
- `npm run preview`: serve the production build locally.
