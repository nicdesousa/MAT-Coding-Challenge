#!/bin/bash
source ../.bash_functions

log "Compiling and packaging the source code"
./mvnw clean compile package -Dquarkus.profile=dev

log "Building the docker image"
# --network=host is sometimes required for the build to use the host DNS resolver
docker build --rm --network=host -f src/main/docker/Dockerfile.amazoncorretto -t nicdesousa/mat-coding-challenge:latest .
