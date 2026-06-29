FROM node:22-alpine AS builder

WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .

ARG VITE_KEYCLOAK_URL
ARG VITE_BACK_END_URL
ARG VITE_WS_URL
ENV VITE_KEYCLOAK_URL=${VITE_KEYCLOAK_URL}
ENV VITE_BACK_END_URL=${VITE_BACK_END_URL}
ENV VITE_WS_URL=${VITE_WS_URL}

RUN npm run build

FROM nginx:stable-alpine

COPY --from=builder /app/dist /usr/share/nginx/html

COPY docker/nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]