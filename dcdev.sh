#!/bin/bash

echo "🐳 Starting Docker Compose Dev Build..."
docker compose -f docker-compose.dev.yml up --build -d
