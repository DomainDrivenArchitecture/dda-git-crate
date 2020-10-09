FROM dda-git-crate

# workaround till new serverspec is available
RUN apt-get install net-tools curl -y

RUN curl -L -o /app/serverspec.jar https://github.com/DomainDrivenArchitecture/dda-serverspec-crate/releases/download/2.0.0/dda-serverspec-standalone.jar
COPY serverspec.edn /app/serverspec.edn

RUN java -jar /app/serverspec.jar /app/serverspec.edn -v
