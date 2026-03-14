FROM node:20-alpine AS builder

WORKDIR /build/frontend-web

RUN corepack enable

COPY frontend-web/package.json frontend-web/pnpm-lock.yaml ./
RUN pnpm install --frozen-lockfile

COPY frontend-web ./
RUN pnpm build

FROM nginx:1.27-alpine

COPY deploy/frontend/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /build/frontend-web/dist /usr/share/nginx/html

EXPOSE 80
