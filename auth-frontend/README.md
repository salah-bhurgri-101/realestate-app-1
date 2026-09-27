# Aurelia Frontend

React 19 + TypeScript + Vite frontend for the Aurelia real-estate platform.

## Features

- Public home page with backend-powered featured properties
- Property search and property details
- Buyer dashboard, wishlist, favorites, and profile
- Seller dashboard, listing creation, listing management, and profile
- Admin dashboard, user management, seller management, property moderation, and audit logs
- Registration with profile fields sent to auth-service and synchronized to user-service through Kafka
- JWT access-token and refresh-token handling
- User profile, settings, address, follow, and block management

## Requirements

- Node.js 20+
- npm
- Running API gateway at `http://localhost:8080`

## Install and run

```powershell
npm.cmd install
npm.cmd run dev
```

Open `http://localhost:5173`.

## Production build

```powershell
npm.cmd run build
npm.cmd run preview
```

The build runs TypeScript compilation followed by Vite production bundling. `npm.cmd run lint` runs the configured ESLint checks.

## API configuration

API clients use the gateway:

```text
http://localhost:8080/api/v1
```

Property requests use the gateway property paths under `/api`. Update the API base URLs in `src/api/axios.ts` and `src/api/propertyApi.ts` when deploying behind a different origin or reverse proxy.

For Vite deployments, set `VITE_API_BASE_URL` (for example,
`https://api.example.com/api/v1`) and `VITE_PROPERTY_API_BASE_URL` (for
example, `https://api.example.com/api/`) at build time.

The frontend stores access and refresh tokens through the auth store and automatically retries one expired access-token request using the refresh-token endpoint.

## Main API clients

- `src/api/authApi.ts` - auth, sessions, admin user operations
- `src/api/userApi.ts` - profile, settings, addresses, follows, blocks
- `src/api/propertyApi.ts` - listings, search, featured properties, images, favorites, reviews
- `src/api/auditApi.ts` - audit log operations

## Production notes

- Serve the generated `dist` directory from a static web server or CDN.
- Configure the gateway CORS origin to the deployed frontend origin.
- Use HTTPS and never hard-code credentials or tokens.
- Ensure the gateway is reachable from the browser while internal services remain private.
