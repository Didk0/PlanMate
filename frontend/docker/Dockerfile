FROM node:22-alpine AS builder

WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .

ARG VITE_KEYCLOAK_URL
ARG VITE_BACK_END_URL
ARG VITE_WS_URL

RUN test -n "$VITE_KEYCLOAK_URL" && test -n "$VITE_BACK_END_URL" && test -n "$VITE_WS_URL" \
    || { echo "ERROR: VITE_KEYCLOAK_URL, VITE_BACK_END_URL and VITE_WS_URL build args are all required"; exit 1; }

ENV VITE_KEYCLOAK_URL=${VITE_KEYCLOAK_URL}
ENV VITE_BACK_END_URL=${VITE_BACK_END_URL}
ENV VITE_WS_URL=${VITE_WS_URL}

RUN npm run build

FROM nginx:stable-alpine

COPY --from=builder /app/dist /usr/share/nginx/html

COPY docker/nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]