package com.jftse.emulator.server.core.service;

import com.jftse.emulator.common.utilities.ResourceUtil;
import com.jftse.entities.database.repository.item.ItemMaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class ItemMaterialService {
    private final ItemMaterialRepository itemMaterialRepository;

    public List<Integer> findAllItemIndexesDB() {
        return itemMaterialRepository.findAllItemIndexes();
    }

    public List<Integer> findAllItemIndexes() {
        List<String> names = new ArrayList<>();

        InputStream inputStream = ResourceUtil.getResource("drops.txt");
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            while (reader.ready()) {
                String name = reader.readLine();
                names.add(name);
            }
        } catch (IOException e) {
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
            }
        }

        List<Integer> itemIndexes = itemMaterialRepository.findAllItemIndexesByNames(names);
        return new ArrayList<>(itemIndexes);
    }
}
