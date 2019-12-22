#!/bin/bash
source ../.bash_functions

log "Compiling and packaging the source code as a native binary"
./mvnw package -Pnative -Dquarkus.native.container-runtime=docker -Dquarkus.profile=dev

log "Building the docker image for the native binary"
# --network=host is sometimes required for the build to use the host DNS resolver
docker build --rm --network=host -f src/main/docker/Dockerfile.native -t nicdesousa/mat-coding-challenge:native .
