FROM openjdk:11


# copy code
WORKDIR app/
COPY target/universal/examsimulator-1.0.zip ./
RUN ["unzip","examsimulator-1.0.zip"]
RUN ["rm","-f" ,"examsimulator-1.0.zip"]

EXPOSE 9000

CMD ["examsimulator-1.0/bin/examsimulator","-Dplay.http.secret.key=ad31779477ee49d5ad5162245429933f3b"]