FROM ubuntu

RUN apt-get update && apt-get upgrade -y && apt-get install openjdk-11-jre apt-utils -y

COPY target/uberjar/dda-git-standalone.jar /app/dda-git-standalone.jar
COPY integration/docker/synced_repo/image/git.edn /app/

RUN useradd -ms /bin/bash initial

RUN java -jar /app/dda-git-standalone.jar /app/git.edn
