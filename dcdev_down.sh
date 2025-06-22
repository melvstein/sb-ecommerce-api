#!/bin/bash

echo "🐳 Running command: docker compose -f docker-compose.dev.yml stop"
docker compose -f docker-compose.dev.yml stop
echo "🐳 Running command: docker compose -f docker-compose.dev.yml down"
docker compose -f docker-compose.dev.yml down