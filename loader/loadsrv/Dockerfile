# docker build -t loadsrv -f ./Dockerfile .
# docker run -i -v /conf/:/loadsrv/conf -p 8087:8087 loadsrv:latest

FROM openjdk:alpine

RUN mkdir          /loadsrv
RUN mkdir          /loadsrv/conf
COPY ./loadsrv.jar /loadsrv/loadsrv.jar

CMD ["java", "-jar", "/loadsrv/loadsrv.jar", "/loadsrv/conf/app.conf"]