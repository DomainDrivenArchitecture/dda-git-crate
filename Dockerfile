FROM ubuntu
RUN apt-get update && apt-get upgrade -y && apt-get -y install sudo openjdk-8-jre gnupg2 openssh-server apt-utils net-tools nano -y
COPY target/dda-git-crate-2.1.0-SNAPSHOT-standalone.jar /app/dda-git-crate.jar
COPY example-git.edn /app/dda-git-crate-config.edn
RUN update-alternatives --set java /usr/lib/jvm/java-8-openjdk-amd64/jre/bin/java
RUN mkdir -p /etc/cron.d/

#add new user and add to sudoers
#RUN useradd -m docker && echo "docker:docker" | chpasswd && adduser docker sudo
#USER docker

RUN java -jar /app/dda-git-crate.jar /app/dda-git-crate-config.edn
