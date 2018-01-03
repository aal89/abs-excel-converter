FROM java:8-alpine

WORKDIR /code

ADD pom.xml /code/pom.xml

# Adding source, compile and package into a fat jar
ADD src /code/src

# Install maven, compile and build the jar
RUN apk --update --no-cache --repository http://dl-cdn.alpinelinux.org/alpine/v3.6/community add maven && \
mvn dependency:resolve && \
mvn verify && \
mvn package && \
apk del maven

CMD ["java", "-jar", "target/service.jar"]
