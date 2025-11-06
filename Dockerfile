 # renovate: datasource=github-releases depName=microsoft/ApplicationInsights-Java
ARG APP_INSIGHTS_AGENT_VERSION=3.7.6
FROM hmctspublic.azurecr.io/base/java:21-distroless

COPY lib/applicationinsights.json /opt/app/
COPY build/libs/opal-legacy-db-stub.jar /opt/app/
COPY wiremock/mappings /opt/app/wiremock/mappings
COPY wiremock/__files /opt/app/wiremock/__files

EXPOSE 4553
CMD [ "opal-legacy-db-stub.jar" ]
