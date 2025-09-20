# Build
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

# Serve
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
# Proxy /api -> API
RUN printf 'server { \
  listen 80; \
  location /api/ { proxy_pass http://host.docker.internal:8080/; proxy_set_header Host $host; } \
  location / { try_files $uri /index.html; } \
}\n' > /etc/nginx/conf.d/default.conf
