FROM openjdk:7-jdk

# Maven
ARG MAVEN_VERSION=3.3.9
ARG USER_HOME_DIR="/root"

RUN mkdir -p /usr/share/maven /usr/share/maven/ref \
  && curl -fsSL http://apache.osuosl.org/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz \
    | tar -xzC /usr/share/maven --strip-components=1 \
  && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

ENV MAVEN_HOME /usr/share/maven
ENV MAVEN_CONFIG "$USER_HOME_DIR/.m2"

# Libs and deps
RUN mkdir /usr/src/ukemi
ADD pom.xml /usr/src/ukemi
WORKDIR /usr/src/ukemi
RUN mvn verify clean --fail-never
RUN mkdir /usr/src/ukemi/lib/
ADD lib/* /usr/src/ukemi/lib/
ADD install_libs.sh /usr/src/ukemi
RUN ./install_libs.sh

# Code
ADD . /usr/src/ukemi
RUN mvn install
ENTRYPOINT ["java -jar /root/.m2/repository/davebcn/ukemi/1.0/ukemi-1.0-jar-with-dependencies.jar . lalala"]
CMD ["java -jar /root/.m2/repository/davebcn/ukemi/1.0/ukemi-1.0-jar-with-dependencies.jar . lalala"]
