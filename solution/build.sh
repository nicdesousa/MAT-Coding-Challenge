#!/bin/bash
set -e
#./mvnw clean compile package -Dquarkus.profile=dev
mvn clean compile package -Dquarkus.profile=dev
docker build --rm -f src/main/docker/Dockerfile.amazoncorretto -t nicdesousa/mat-coding-challenge .
