#!/bin/bash
source .bash_functions

log "Updating the source code"
git pull

log "Cleaning the docker-compose state"
docker-compose down --remove-orphans

(
  cd solution
  ./build.sh
)

log "Starting docker-compose"
docker-compose up

log "Cleaning the docker-compose state"
docker-compose down --remove-orphans
