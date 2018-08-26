# docker build -t loadsrv -f ./Dockerfile .
# docker run -i loadsrv:latest

FROM openjdk:alpine

RUN mkdir          /loadsrv
COPY ./loadsrv.jar /loadsrv/loadsrv.jar
COPY ./app.conf    /loadsrv/app.conf
COPY ./credsqs     /loadsrv/credsqs

CMD ["java", "-jar", "/loadsrv/loadsrv.jar", "/loadsrv/app.conf"]
