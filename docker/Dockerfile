FROM maven:3.6.3-openjdk-15-slim
ARG NETWORK_IP

WORKDIR /jftse
COPY . .
RUN sed -i "s/localhost/db/g" src/main/resources/application.properties
RUN sed -i "s/127.0.0.1/$NETWORK_IP/g" src/main/java/com/ft/emulator/server/game/core/game/handler/GamePacketHandler.java
RUN mvn clean install

WORKDIR /jftse/target
CMD ["java","-jar","ft_server_emulator.jar"]