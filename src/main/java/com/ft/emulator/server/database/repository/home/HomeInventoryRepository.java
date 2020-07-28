package com.ft.emulator.server.database.repository.home;

import com.ft.emulator.server.database.model.home.AccountHome;
import com.ft.emulator.server.database.model.home.HomeInventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HomeInventoryRepository extends JpaRepository<HomeInventory, Long> {
    List<HomeInventory> findAllByAccountHome(AccountHome accountHome);
}
