package com.ft.emulator.server.authserver;

import com.ft.emulator.server.database.model.account.Account;
import com.ft.emulator.server.database.model.gameserver.GameServer;
import com.ft.emulator.common.service.Service;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.List;

public class AuthenticationImpl extends Service {

    public AuthenticationImpl(EntityManagerFactory entityManagerFactory) {

        super(entityManagerFactory);
    }

    public Account login(String username, String password) {

	EntityManager em = this.entityManagerFactory.createEntityManager();

	String sql = "FROM Account a LEFT JOIN FETCH a.characterPlayerList cpl WHERE a.username = :username AND a.password = :password ";
	List<Account> accountList = em.createQuery(sql, Account.class)
		.setParameter("username", username)
		.setParameter("password", password)
		.getResultList();

	em.close();

	return accountList.size() != 1 ? null : accountList.get(0);
    }

    public List<GameServer> getGameServerList() {

	EntityManager em = this.entityManagerFactory.createEntityManager();

	List<GameServer> gameServerList = em.createQuery("FROM GameServer gs LEFT JOIN FETCH gs.gameServerType gst", GameServer.class)
		.getResultList();

	em.close();

	return gameServerList;
    }
}