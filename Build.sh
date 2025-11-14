#! /bin/bash

echo "Cleaning and building"
echo "Installing project"
echo "Building docker image"
mvn clean package && docker build -t bitdata_app -f Dockerfile . && docker compose down --remove-orphans &&  docker compose up

## -Dmaven.test.skip=true -DskipTests