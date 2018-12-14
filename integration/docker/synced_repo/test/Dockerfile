FROM domaindrivenarchitecture/dda-git-crate:synced-repo-SNAPSHOT

# workaround till new serverspec is available
RUN apt-get install net-tools curl -y

RUN curl -L -o /app/serverspec.jar https://github.com/DomainDrivenArchitecture/dda-serverspec-crate/releases/download/1.2.1/dda-serverspec-crate-1.2.1-standalone.jar
COPY integration/docker/synced_repo/test/serverspec.edn /app/serverspec.edn

RUN java -jar /app/serverspec.jar /app/serverspec.edn -v
