# JFTSE - Java Fantasy Tennis Server Emulator

![status in development](https://img.shields.io/badge/Status-In%20development-orange)

--------------

* [Introduction](#introduction)
* [Requirements](#requirements)
* [Installation](#installation)
* [Running the emulator](#running-the-emulator)
* [Reporting issues](#reporting-issues)
* [Submitting fixes](#submitting-fixes)
* [Copyright](#copyright)
* [Footnotes](#footnotes)

## Introduction

**JFTSE** is an open source project for the game **Fantasy Tennis** based on [**AnCoFT**](https://github.com/AnCoFT/AnCoFT).    

Its is based on AnCoFT, because back then, I started writing a private server as well, due to my lack of experience in that topic I abandoned it, in that time.    
I'm really thankful that AnCoFT started that project as well and reversed the structure of how packets are working etc. and released it! Due to him I could continue and use/utilize packet sniffs I made back then (which has more stuff than AnCoFT project)    

Like the title says, it's a server emulator and written in Java.

It is completely open source; community involvement is highly encouraged.


## Requirements

Since it's cross-platform I will not provide download links otherwise I will bloat this. If you use Windows then download them for Windows. If for Linux then download them for Linux.

| Name | Version |
|------|---------|
| JDK / OpenJDK | 15 / 15 |
| Maven | ≥ 3.6.3 |
| MySQL | 8.0 |
| Any Java capable IDE [^1] | Any Version |
| Fantasy Tennis Thai | 1.706 |

* Also you need a Git CLI or GUI. Doesn't matter which one.

**_Note:_** If under Windows, Maven & JDK has to be configured in your PATH variable. 

## Installation

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

Before you run it the first time, please execute[^2] the SQL file **_create_fantasytennis.sql_** located inside **sql/create/**.

Build the emulator and run it via:
```
cd target
java -jar ft_server_emulator.jar
```
Or you run it from inside your Java IDE if using one.

The emulator will do his first time initialization and the process will take some time. It loades static data like products of the shop etc.    
When it says 
> **Emulator successfully started!**

Then the initialization was successful and the server is running.

Before you start to play, you have to do 3 more things:
1. Execute[^2] the SQL file **__gameservertype.sql__** located inside **docker/sql/insert/** (execute this first!)
2. Execute[^2] the SQL file **__gameserver.sql__** located inside **docker/sql/insert/**
3. Execute[^2] the SQL file **__account.sql__** located inside **docker/sql/insert/**

### Running it

You run the emulator via:
```
cd target
java -jar ft_server_emulator.jar
```

## Running client
1. Download FT Client from : https://www.jftse.com/client/FantaTennis.7z
2. Unzip and execute it for the first time in order to create configuration files
3. Edit 'Serverinfo.ini' file to replace the IP (127.0.0.1) and Port (5894)
4. Disable AntiCheat under GlobalSettings.java with IsAntiCheatEnabled=false. A new build is required in order to take this modification
5. Start the client with 'FantaTennis.exe' and log in with 'test/test'

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
