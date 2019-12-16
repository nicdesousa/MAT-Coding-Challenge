#!/bin/bash
set -e
docker-compose down --remove-orphans
(
  cd solution
  ./build.sh
)
docker-compose up
docker-compose down --remove-orphans
