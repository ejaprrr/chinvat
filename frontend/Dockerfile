# syntax=docker/dockerfile:1.7

# Build stage only - outputs dist/ folder for integration
FROM node:20-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npm run build

# Default to builder stage - copy dist/ from this image
FROM scratch
COPY --from=build /app/dist /dist
