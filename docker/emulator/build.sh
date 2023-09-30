#!/bin/bash

mkdir -p /opt/java
mkdir -p /opt/maven
echo "Downloading openjdk-21..."
curl -LO https://download.java.net/java/GA/jdk21/fd2272bbf8e04c3dbaee13770090416c/35/GPL/openjdk-21_linux-x64_bin.tar.gz \
    && tar xf openjdk-21_linux-x64_bin.tar.gz \
    && mv jdk-21 /opt/java \
    && rm openjdk-21_linux-x64_bin.tar.gz

echo "Downloading maven-3.9.4..."
curl -LO https://dlcdn.apache.org/maven/maven-3/3.9.4/binaries/apache-maven-3.9.4-bin.tar.gz \
    && tar xf apache-maven-3.9.4-bin.tar.gz \
    && mv apache-maven-3.9.4 /opt/maven \
    && rm apache-maven-3.9.4-bin.tar.gz