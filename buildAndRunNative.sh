#!/bin/bash
source .bash_functions

log "Updating the source code"
git pull

log "Configuring the environment for a native binary build"
export TELEMETRY_IMAGE_TAG=native

log "Cleaning the docker-compose state"
docker-compose down --remove-orphans

(
  cd solution
  ./buildNative.sh
)

log "Starting docker-compose"
docker-compose up

log "Cleaning the docker-compose state"
docker-compose down --remove-orphans
