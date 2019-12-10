package com.ft.emulator;

import com.ft.emulator.common.service.EntityManagerFactoryUtil;
import com.ft.emulator.server.authserver.LoginServer;
import com.ft.emulator.server.game.server.GameServer;

import com.ft.emulator.server.shared.module.DbDataLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManagerFactory;

import java.util.Scanner;

public class StartApplication {

    private final static Logger logger = LoggerFactory.getLogger("main");

    public static void main(String[] args) {

        logger.info("--------------------------------------");
        logger.info("JFTSE - Fantasy Tennis Server Emulator");
        logger.info("--------------------------------------\n\n");

        // init db
        logger.info("Initializing database...");
        EntityManagerFactory emFactory = EntityManagerFactoryUtil.INSTANCE.getEntityManagerFactory();
        logger.info("Successfully initialized!");

        // load first time data
        logger.info("--------------------------------------");
        logger.info("Loading data into the database...");
        new DbDataLoader();
        logger.info("--------------------------------------");

        // init login server
        logger.info("Initializing authentication server...");
        LoginServer loginServer = new LoginServer(5894);
        try {

            loginServer.start();
        }
        catch (Exception e) {
            logger.error("Failed to start authentication server!");
            e.printStackTrace();
            System.exit(1);
        }
        logger.info("Successfully initialized!");
        logger.info("--------------------------------------");

        // init game server
        logger.info("Initializing game server...");
        GameServer gameServer = new GameServer(5895);
        try {

            gameServer.start();
        }
        catch (Exception e) {
            logger.error("Failed to start game server!");
            e.printStackTrace();
            System.exit(1);
        }
        logger.info("Successfully initialized!");
        logger.info("--------------------------------------");

        logger.info("Emulator successfully started!");
        logger.info("Write exit and confirm with enter to stop the emulator!");

        Scanner scan = new Scanner(System.in);
        String input;
        while(true) {
            input = scan.next();

            if(input.equals("exit"))
                break;
        }

        loginServer.stop();
        gameServer.stop();

        EntityManagerFactoryUtil.INSTANCE.close();
    }
}