package com.ft.emulator.server.game.home;

import com.ft.emulator.common.dao.GenericModelDao;
import com.ft.emulator.common.service.Service;
import com.ft.emulator.server.database.model.account.Account;
import com.ft.emulator.server.database.model.home.AccountHome;
import com.ft.emulator.server.database.model.home.HomeInventory;

import javax.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeImpl extends Service {

    private GenericModelDao<AccountHome> accountHomeDao;
    private GenericModelDao<HomeInventory> homeInventoryDao;

    public HomeImpl(EntityManagerFactory entityManagerFactory) {

        super(entityManagerFactory);

        accountHomeDao = new GenericModelDao<>(entityManagerFactory, AccountHome.class);
        homeInventoryDao = new GenericModelDao<>(entityManagerFactory, HomeInventory.class);
    }

    public AccountHome getAccountHome(Account account) {

	Map<String, Object> filter = new HashMap<>();
	filter.put("account", account);
	return accountHomeDao.find(filter);
    }

    public List<HomeInventory> getHomeInventoryList(AccountHome accountHome) {

	Map<String, Object> filter = new HashMap<>();
	filter.put("accountHome", accountHome);
	return homeInventoryDao.getList(filter);
    }

    public void removeItemFromHomeInventory(Long homeInventoryId) {
        homeInventoryDao.remove(homeInventoryId);
    }
}