package com.jftse.entities.database.repository.home;

import com.jftse.entities.database.model.home.AccountHome;
import com.jftse.entities.database.model.home.HomeInventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HomeInventoryRepository extends JpaRepository<HomeInventory, Long> {
    List<HomeInventory> findAllByAccountHome(AccountHome accountHome);
}
