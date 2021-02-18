package com.jftse.emulator.connector.rest.controller;

import com.jftse.emulator.server.database.model.item.ItemEnchant;
import com.jftse.emulator.server.database.model.item.ItemHouse;
import com.jftse.emulator.server.database.repository.item.ItemEnchantRepository;
import com.jftse.emulator.server.database.repository.item.ItemHouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SampleController {

    @Autowired
    private ItemHouseRepository itemHouseRepository;
    @Autowired
    private ItemEnchantRepository itemEnchantRepository;

    @GetMapping("house")
    public List<ItemHouse> getItemHouseList() {
        return itemHouseRepository.findAll();
    }

    @GetMapping("enchant")
    public List<ItemEnchant> getItemEnchantList() {
        return itemEnchantRepository.findAll();
    }
}