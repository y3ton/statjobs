# docker build -t loadsrv -f ./Dockerfile .
FROM openjdk:alpine

RUN mkdir          /loadsrv
COPY ./loadsrv.jar /loadsrv/loadsrv.jar
COPY ./app.conf    /loadsrv/app.conf
COPY ./credsqs     /loadsrv/credsqs

ENTRYPOINT java -jar /loadsrv/loadsrv.jar /loadsrv/app.conf
