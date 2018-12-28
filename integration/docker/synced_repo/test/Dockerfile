FROM dda-git-crate

# workaround till new serverspec is available
RUN apt-get install net-tools curl -y

RUN curl -L -o /app/serverspec.jar https://github.com/DomainDrivenArchitecture/dda-serverspec-crate/releases/download/1.3.0/dda-serverspec-standalone.jar
COPY integration/docker/synced_repo/test/serverspec.edn /app/serverspec.edn

RUN java -jar /app/serverspec.jar /app/serverspec.edn -v
