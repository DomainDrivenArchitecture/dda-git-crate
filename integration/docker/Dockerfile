FROM ubuntu
RUN apt-get update && apt-get upgrade -y && apt-get install openjdk-8-jre apt-utils net-tools -y
COPY target/dda-git-crate-2.1.4-SNAPSHOT-standalone.jar /app/dda-git-crate.jar
COPY example-git.edn /app/dda-git-crate-config.edn

RUN mkdir -p /etc/cron.d/
RUN useradd -ms /bin/bash initial

RUN java -jar /app/dda-git-crate.jar /app/dda-git-crate-config.edn
