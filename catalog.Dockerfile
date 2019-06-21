# Build Catalog Server Image
FROM openjdk:8u181-jre-alpine3.8

# Copy local catalog config for running locally
COPY ./workivabuild/local-catalog-config.edn /local-catalog-config.edn

# Install bash
RUN apk add --update bash nss

# Copy over jar and required scripts
WORKDIR /opt
COPY ./server.alpha/target/server.alpha*.jar /opt/
COPY ./scripts/run_service.sh /usr/local/bin/run_service.sh
COPY ./scripts/set_mem_constraints.sh /usr/local/bin/set_mem_constraints.sh

RUN chmod +x /usr/local/bin/run_service.sh
RUN chmod +x /usr/local/bin/set_mem_constraints.sh

### Update Packages for Security Compliance
ARG BUILD_ID
RUN apk update && apk upgrade

USER nobody
CMD [ "/usr/local/bin/run_service.sh" ]
