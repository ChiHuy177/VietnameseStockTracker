# --- Build stage: produce static files in dist/ ---
FROM node:22-alpine AS build
WORKDIR /build

COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build

# --- Runtime stage: nginx serves dist/ and proxies /api, /ws to the backend ---
# (replaces the Vite dev-server proxy in vite.config.ts, which only exists under `npm run dev`)
FROM nginx:1.27-alpine

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /build/dist /usr/share/nginx/html

EXPOSE 80
