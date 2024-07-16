# JFTSE - Java Fantasy Tennis Server Emulator

![status in development](https://img.shields.io/badge/Status-In%20development-orange)

--------------

* [Introduction](#introduction)
* [Requirements](#requirements)
* [Project structure](#project-structure)
* [Installation](#installation)
* [Running the emulator](#running-the-emulator)
* [Reporting issues](#reporting-issues)
* [Submitting fixes](#submitting-fixes)
* [Copyright](#copyright)
* [Footnotes](#footnotes)

## Introduction

**JFTSE** is an open source project for the game **Fantasy Tennis**.    

It's a server emulator and written in Java.

It is completely open source; community involvement is highly encouraged.


## Requirements

Since it's cross-platform I will not provide download links otherwise I will bloat this. If you use Windows then download them for Windows. If for Linux then download them for Linux.

| Name | Version     |
|------|-------------|
| JDK / OpenJDK | 21 / 21     |
| Maven | ≥ 3.6.3     |
| MySQL | 8.0         |
| RabbitMQ | 3.12        |
| Any Java capable IDE [^1] | Any Version |
| Fantasy Tennis Thai | 1.706       |

* Also you need a Git CLI or GUI. Doesn't matter which one.

**_Note:_** If under Windows, Maven & JDK has to be configured in your PATH variable. 

## Project structure

The project is divided into 12 modules:

----------------
* **emulator** - Old emulator, will be removed in the future
* **entities** - Database model, repository and converters
* **commons** - Utility classes
* **commons-proto** - Protocol buffer classes
* **server-core** - The core of the server containing abstract networking classes, protocol, codec, service classes for DB access, shared enums and constants needed by server implementations
* **b2b-webservice** - RESTful web service for the B2B API
* **auth-server** - Login server implementation
* **game-server** - Game server implementation (lobby, singleplay, matchplay, etc.)
* **chat-server** - Chat server implementation (chat lobby & rooms)
* **relay-server** - Relay server implementation (match interaction between players has to be broadcast to all players in the match)
* **ac-server** - Anti-cheat server implementation (JFTSE only)
* **docker** - Docker files for the server (needed and not needed ones for running the server)

## Installation

### Installing docker

For downloading and installing docker on your host, please refer to the official documentation: [Get Docker | Docker Documentation](https://docs.docker.com/get-docker/)

Using WSL2 (with Ubuntu 20.04) is recommended for Windows users which are developing on Windows. ([How to install Linux on Windows with WSL](https://learn.microsoft.com/en-us/windows/wsl/install))

After WSL2 is installed, you can install docker inside WSL2 with the following commands: (those commands can be run on any Linux distribution with apt package manager)
```
# Update the apt package index
sudo apt-get update
sudo apt-get upgrade

# Install packages to allow apt to use a repository over HTTPS:
sudo apt-get install apt-transport-https ca-certificates curl gnupg lsb-release

# Add Docker’s official GPG key:
sudo mkdir -p /etc/apt/keyrings
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg

# Update the apt package index and install the latest version of Docker Engine and containerd
sudo apt-get update
sudo apt-get install docker-ce docker-ce-cli containerd.io

# Check if docker is running
sudo service docker status
```

### Setting up the RabbitMQ server

For downloading and installing RabbitMQ on your host, please refer to the official documentation: [ Downloading and Installing RabbitMQ
— RabbitMQ](https://www.rabbitmq.com/download.html)

We **recommend** using the docker image for RabbitMQ, since it's the easiest way to get it up and running.  
You can find the docker setup in the **docker** folder of the root project directory. There is a **docker-compose.yml** file which contains the configuration for the RabbitMQ server. The subfolder **rabbitmq** contains the configuration files and the **Dockerfile** for the RabbitMQ server.

#### Building the RabbitMQ server

To build the RabbitMQ server, you have to execute the following command in the **docker** folder of the root project directory:
```
docker compose build --no-cache rabbitmq
```

#### Running the RabbitMQ server

To run the RabbitMQ server, you have to execute the following command in the **docker** folder of the root project directory:
```
docker compose up rabbitmq -d
```

The RabbitMQ server is now running and is reachable through port 5672 and the 15672 port (management interface). The management interface is a web interface and can be accessed through your browser (localhost:15672). The default username and password is **guest**.

#### Configuring the RabbitMQ server

It is recommended to adjust the **definitions.json** file in the **rabbitmq** folder inside the **docker** folder of the root project directory.  
The file contains configuration for users, vhosts and permissions (refer to official documentation for all available options). Currently the default configuration is used, which is **not recommended for production use**.

Therefore you should adjust the configuration to your needs. Getting a hashed password for a user is easy and can be done inside the rabbitmq container.  
First you need to connect to your running rabbitmq container:
```
docker exec -it rabbitmq-server bash
```
Then you can use the **rabbitmqctl** command to generate a hashed password for a user:
```
rabbitmqctl hash_password <password>
```

With **CTRL + C** or typing **exit** you can exit the container.

The output of the command is the hashed password, which you can use in the **definitions.json** file.  
That same password and username (defined in **definitions.json**) has to be used in the **application.properties** file of the **chat-server** and **game-server** module. The host and port inside **application.properties** has not to be adjusted unless you changed the host and port configuration of the RabbitMQ server.

You need to restart the RabbitMQ server after you adjusted the **definitions.json** file:
```
docker compose restart rabbitmq
```

### Building the server itself

#### Getting the source code

```
git clone -b master git://github.com/sstokic-tgm/JFTSE.git
```
This will clone master branch, this is the  **RECOMMENDED**  branch for starters.

### Compiling the source code

#### Building the core

```
cd <path to the recently cloned project>
mvn clean install
```
This will compile and build the core.

#### Keeping the code up to date

To update the core files, do the following:
```
cd <path to the cloned project>
# For master branch
git pull origin master
```
Afterwards you can build the emulator:
```
mvn clean install
```

## Running the emulator

### Running it the first time

Before you run it the first time, please execute[^2] the SQL file **_create_fantasytennis.sql_** located inside **emulator/sql/create/**.

Build the emulator and run it via:
```
cd auth-server/target
java -jar auth-server-1.0.0-SNAPSHOT.jar
```
Or you run it from inside your Java IDE if using one.

The auth-server will do his first time initialization and the process will take some time. It loades static data like products of the shop etc.    
When it says 
> **auth-server successfully started!**

Then the initialization was successful and the auth-server is running.

Before you start to play, you have to do 3 more things:
1. Execute[^2] the SQL file **__gameservertype.sql__** located inside **emulator/sql/insert/** (execute this first!)
2. Execute[^2] the SQL file **__gameserver.sql__** located inside **emulator/sql/insert/**
3. Create a new account inside the Account table

Then you can start other server implementations you need or want to play on:
```
cd game-server/target
java -jar game-server-1.0.0-SNAPSHOT.jar
```

The relay-server is needed for playing since it relays/broadcasts the match interaction between players to all players in the match:
```
cd relay-server/target
java -jar relay-server-1.0.0-SNAPSHOT.jar
```

### Running it

You run the emulators via:
```
cd xxx-server/target
java -jar xxx-server-1.0.0-SNAPSHOT.jar
```

## Running client
1. Download FT Client from : https://www.jftse.com/client/FantaTennis.7z
2. Unzip and execute it for the first time in order to create configuration files
3. Edit 'Serverinfo.ini' file to replace the IP (127.0.0.1) and Port (5894)
4. Start the client with 'FantaTennis.exe' and log in with the created account

## Reporting issues

_TODO_

## Submitting fixes

_TODO_

## Copyright

License: GPL 3.0    
Read file [LICENSE](LICENSE).

## Footnotes

[^1]: If you want to develop yourself.
[^2]: You do it inside MySQL. Via CLI or some GUI application, it doesn't matter.
