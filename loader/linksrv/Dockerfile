# docker build -t linksrv -f ./Dockerfile .
# docker run -i -v /conf/:/linksrv/conf -p 8080:8080 linksrv:latest

FROM openjdk:alpine

RUN mkdir          /linksrv
RUN mkdir          /linksrv/conf
COPY ./linksrv.jar /linksrv/linksrv.jar

CMD ["java", "-jar", "/linksrv/linksrv.jar", "/linksrv/conf/app.conf"]