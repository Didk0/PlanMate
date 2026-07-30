FROM quay.io/keycloak/keycloak:26.1.0 AS builder

ENV KC_DB=postgres
ENV KC_HEALTH_ENABLED=true
ENV KC_METRICS_ENABLED=false
ENV KC_TRANSACTION_XA_ENABLED=false

COPY --chown=1000:0 themes/planmate /opt/keycloak/themes/planmate

RUN /opt/keycloak/bin/kc.sh build

FROM quay.io/keycloak/keycloak:26.1.0

COPY --from=builder --chown=1000:0 /opt/keycloak/ /opt/keycloak/
COPY --chown=1000:0 planmate-realm.json /opt/keycloak/data/import/planmate-realm.json

ENV KC_HTTP_ENABLED=true
ENV KC_HTTP_PORT=8080
ENV KC_PROXY_HEADERS=xforwarded

ENV KC_CACHE=local

ENV JAVA_OPTS_KC_HEAP="-Xms96m -Xmx300m -XX:MaxMetaspaceSize=160m -Xss512k"

ENV KC_DB_POOL_INITIAL_SIZE=1
ENV KC_DB_POOL_MIN_SIZE=0
ENV KC_DB_POOL_MAX_SIZE=5

EXPOSE 8080

CMD ["start", "--optimized", "--import-realm"]
