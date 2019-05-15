# Setup Phase
FROM clojure:openjdk-8-lein-2.8.3-alpine as setup

## Pre-fetch dependencies
WORKDIR /prefetch
COPY ./project.clj /prefetch/project.clj
COPY ./workivabuild/prefetch-deps /prefetch/workivabuild/prefetch-deps
RUN lein modules :dirs workivabuild/prefetch-deps deps

## Build Modules Phase
FROM clojure:openjdk-8-lein-2.8.3-alpine as build_modules
### Copy Leiningen Profile
COPY --from=setup /root/.lein /root/.lein
### Copy pre-fetched local Maven repo
COPY --from=setup /root/.m2/repository /root/.m2/repository

## Build project
WORKDIR /build_modules
COPY . /build_modules/

### Remove workivabuild/prefetch project
RUN rm -rf ./workivabuild/prefetch-deps
### Lint, Test and install modules in dependency order
RUN lein modules do cljfmt check, test, install
### Capture all jars that are produced in all modules
ARG BUILD_ARTIFACTS_JAVA=/build_modules/**/target/*.jar

### Build Documentation
RUN cd ./documentation && tar cvfz "../eva-catalog-docs.tgz" ./
ARG BUILD_ARTIFACTS_DOCUMENTATION=/build_modules/eva-catalog-docs.tgz

# * Build Server Phase
FROM clojure:openjdk-8-lein-2.8.3-alpine as build_server

COPY --from=build_modules /root/.lein /root/.lein
COPY --from=build_modules /root/.m2/repository /root/.m2/repository
COPY ./project.clj /build_server/project.clj
COPY ./server.alpha /build_server/server.alpha

WORKDIR /build_server

RUN lein modules :dirs server.alpha uberjar

# Prepare Veracode Artifact
FROM clojure:openjdk-8-lein-2.8.3-alpine AS veracode

COPY --from=build_modules /root/.lein /root/.lein
COPY --from=build_modules /root/.m2/repository /root/.m2/repository
COPY --from=build_server /build_server /build_server

WORKDIR /build_server

## Compile Service Jar with Debugging
RUN mkdir ./veracode
RUN cd ./server.alpha && lein with-profile +debug-compile uberjar
RUN cd ./server.alpha/target && tar czf ../../veracode/clojure.tar.gz ./eva-catalog-server.jar

## Declare Artifact
ARG BUILD_ARTIFACTS_VERACODE=/build_server/veracode/clojure.tar.gz

# Build Catalog Server Image
FROM openjdk:8u181-jre-alpine3.8

# Copy local catalog config for running locally
COPY ./workivabuild/local-catalog-config.edn /local-catalog-config.edn

# Install bash
RUN apk add --update bash nss

# Copy over jar and required scripts
WORKDIR /opt
COPY --from=build_server /build_server/server.alpha/target/eva-catalog-server.jar /opt/eva-catalog-server.jar
COPY ./scripts/run_service.sh /usr/local/bin/run_service.sh
COPY ./scripts/set_mem_constraints.sh /usr/local/bin/set_mem_constraints.sh

RUN chmod +x /usr/local/bin/run_service.sh
RUN chmod +x /usr/local/bin/set_mem_constraints.sh

### Update Packages for Security Compliance
ARG BUILD_ID
RUN apk update && apk upgrade

USER nobody
CMD [ "/usr/local/bin/run_service.sh" ]
