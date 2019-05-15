#!/usr/bin/env bash

set -ex

docker-compose \
    -f ./docker/docker-compose.yml \
    -f ./integration-testing/skynet/docker-compose.skynet.override.yml \
    up -d
docker-compose \
    -f ./docker/docker-compose.yml \
    -f ./integration-testing/skynet/docker-compose.skynet.override.yml logs \
    -f eva-catalog > logs/eva-catalog.log 2>&1 &

wget -q -O /dev/stdout \
    --timeout=30 \
    --tries=10 \
    --retry-connrefused \
    --content-on-error http://eva-catalog:3000/status
