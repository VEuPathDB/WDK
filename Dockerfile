# # # # # # # # # # # # # # # # # # # # # # # # # # # # # #
#
#   Build Service & Dependencies
#
# # # # # # # # # # # # # # # # # # # # # # # # # # # # # #
FROM veupathdb/alpine-dev-base:jdk-18 AS prep

LABEL service="wdk-sample-build"

ARG GITHUB_USERNAME
ARG GITHUB_TOKEN

RUN apk add --no-cache git sed findutils coreutils make npm curl gawk jq openssh git apache-ant maven perl \
  && git config --global advice.detachedHead false

WORKDIR /gusApp

RUN mkdir gus_home
RUN mkdir project_home

ENV GUS_HOME=/gusApp/gus_home
ENV PROJECT_HOME=/gusApp/project_home
ENV PATH=$PROJECT_HOME/install/bin:$GUS_HOME/bin:$PATH

RUN cd project_home \
  && git clone https://github.com/VEuPathDB/install.git \
  && git clone https://github.com/VEuPathDB/FgpUtil.git \
  && git clone https://github.com/VEuPathDB/WSF.git \
  && git clone https://github.com/VEuPathDB/WDK.git

RUN mkdir /gusApp/gus_home/config && cp /gusApp/project_home/install/config/gus.config.sample /gusApp/gus_home/config/gus.config

RUN bld WDK


# # # # # # # # # # # # # # # # # # # # # # # # # # # # # #
#
#   Run the service
#
# # # # # # # # # # # # # # # # # # # # # # # # # # # # # #
FROM amazoncorretto:18-alpine3.15

LABEL service="wdk-sample"

RUN apk add --no-cache tzdata bash git apache-ant maven perl npm \
    && cp /usr/share/zoneinfo/America/New_York /etc/localtime \
    && echo "America/New_York" > /etc/timezone

ENV GUS_HOME=/gusApp/gus_home
ENV PROJECT_HOME=/gusApp/project_home
ENV PATH=$PROJECT_HOME/install/bin:$GUS_HOME/bin:$PATH

COPY --from=prep /gusApp/gus_home /gusApp/gus_home
COPY --from=prep /gusApp/project_home /gusApp/project_home
COPY --from=prep /root/.m2/repository /root/.m2/repository

CMD bld WDK

