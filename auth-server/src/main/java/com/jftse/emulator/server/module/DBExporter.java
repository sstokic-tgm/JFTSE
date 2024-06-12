package com.jftse.emulator.server.module;

import com.jftse.entities.database.model.battle.BossGuardian;
import com.jftse.entities.database.model.battle.Guardian;
import com.jftse.entities.database.model.battle.GuardianBase;
import com.jftse.entities.database.model.item.Product;
import com.jftse.entities.database.repository.battle.BossGuardianRepository;
import com.jftse.entities.database.repository.battle.GuardianRepository;
import com.jftse.entities.database.repository.item.ProductRepository;
import lombok.extern.log4j.Log4j2;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j2
public class DBExporter {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private BossGuardianRepository bossGuardianRepository;
    @Autowired
    private GuardianRepository guardianRepository;

    public void init() {
        log.info("Exporting Shop_Ini3.xml...");

        if (!Files.exists(Paths.get("res/Shop_Ini3_export.xml"))) {
            try {
                Files.copy(Paths.get("res/Shop_Ini3.xml"), Paths.get("res/Shop_Ini3_export.xml"));
            } catch (IOException e) {
                log.error("Error copying xml file", e);
            }
        }

        try {
            exportProducts(productRepository.findAll(), "res/Shop_Ini3_export.xml");
        } catch (IOException e) {
            log.error("Error updating xml file", e);
        } catch (DocumentException e) {
            log.error("Error reading xml file", e);
        }

        if (!Files.exists(Paths.get("res/GuardianInfo_export.xml"))) {
            try {
                Files.copy(Paths.get("res/GuardianInfo.xml"), Paths.get("res/GuardianInfo_export.xml"));
            } catch (IOException e) {
                log.error("Error copying xml file", e);
            }
        }

        try {
            exportGuardians(guardianRepository.findAll(), "res/GuardianInfo_export.xml");
        } catch (IOException e) {
            log.error("Error updating xml file", e);
        } catch (DocumentException e) {
            log.error("Error reading xml file", e);
        }

        if (!Files.exists(Paths.get("res/BossGuardianInfo_Ini3_export.xml"))) {
            try {
                Files.copy(Paths.get("res/BossGuardianInfo_Ini3.xml"), Paths.get("res/BossGuardianInfo_Ini3_export.xml"));
            } catch (IOException e) {
                log.error("Error copying xml file", e);
            }
        }

        try {
            exportBossGuardians(bossGuardianRepository.findAll(), "res/BossGuardianInfo_Ini3_export.xml");
        } catch (IOException e) {
            log.error("Error updating xml file", e);
        } catch (DocumentException e) {
            log.error("Error reading xml file", e);
        }
    }

    private void exportProducts(List<Product> products, String filePath) throws IOException, DocumentException {
        SAXReader reader = new SAXReader();
        Document document = reader.read(filePath);

        Element productListElement = document.getRootElement();

        for (Product product : products) {
            Element productElement = findProductElement(product.getProductIndex(), productListElement);

            if (productElement != null) {
                productElement.attribute("DISPLAY").setValue(String.valueOf(product.getDisplay()));
                productElement.attribute("HIT_DISPLAY").setValue(product.getHitDisplay() ? "1" : "0");
                productElement.attribute("Enable").setValue(product.getEnabled() ? "1" : "0");
                productElement.attribute("UseType").setValue(product.getUseType());
                productElement.attribute("Use0").setValue(String.valueOf(product.getUse0()));
                productElement.attribute("Use1").setValue(String.valueOf(product.getUse1()));
                productElement.attribute("Use2").setValue(String.valueOf(product.getUse2()));
                productElement.attribute("PriceType").setValue(product.getPriceType());
                productElement.attribute("OldPrice0").setValue(String.valueOf(product.getOldPrice0()));
                productElement.attribute("OldPrice1").setValue(String.valueOf(product.getOldPrice1()));
                productElement.attribute("OldPrice2").setValue(String.valueOf(product.getOldPrice2()));
                productElement.attribute("Price0").setValue(String.valueOf(product.getPrice0()));
                productElement.attribute("Price1").setValue(String.valueOf(product.getPrice1()));
                productElement.attribute("Price2").setValue(String.valueOf(product.getPrice2()));
                productElement.attribute("CouplePrice").setValue(String.valueOf(product.getCouplePrice()));
                productElement.attribute("Name").setValue(product.getName());
                productElement.attribute("GoldBack").setValue(String.valueOf(product.getGoldBack()));
                productElement.attribute("EnableParcel").setValue(product.getEnableParcel() ? "1" : "0");
                productElement.attribute("Item0").setValue(String.valueOf(product.getItem0()));
                productElement.attribute("Item1").setValue(String.valueOf(product.getItem1()));
                productElement.attribute("Item2").setValue(String.valueOf(product.getItem2()));
                productElement.attribute("Item3").setValue(String.valueOf(product.getItem3()));
                productElement.attribute("Item4").setValue(String.valueOf(product.getItem4()));
                productElement.attribute("Item5").setValue(String.valueOf(product.getItem5()));
                productElement.attribute("Item6").setValue(String.valueOf(product.getItem6()));
                productElement.attribute("Item7").setValue(String.valueOf(product.getItem7()));
                productElement.attribute("Item8").setValue(String.valueOf(product.getItem8()));
                productElement.attribute("Item9").setValue(String.valueOf(product.getItem9()));

                log.debug("Product updated: " + product.getProductIndex() + " - " + product.getName());
            }
        }

        try (FileWriter fileWriter = new FileWriter(filePath)) {
            OutputFormat format = OutputFormat.createPrettyPrint();
            XMLWriter writer = new XMLWriter(fileWriter, format);
            writer.write(document);
            writer.flush();
        }
    }

    private void exportGuardians(List<Guardian> guardianList, String filePath) throws IOException, DocumentException {
        List<GuardianBase> guardians = guardianList.stream().map(g -> g).collect(Collectors.toList());
        exportGuards(guardians, filePath);
    }

    private void exportBossGuardians(List<BossGuardian> bossGuardianList, String filePath) throws IOException, DocumentException {
        List<GuardianBase> guardians = bossGuardianList.stream().map(g -> g).collect(Collectors.toList());
        exportGuards(guardians, filePath);
    }

    private void exportGuards(List<GuardianBase> guardianList, String filePath) throws IOException, DocumentException {
        SAXReader reader = new SAXReader();
        Document document = reader.read(filePath);

        Element guardianListElement = document.getRootElement();

        for (GuardianBase guardian : guardianList) {
            Element guardianElement = findGuardianElement(Math.toIntExact(guardian.getId()), guardianListElement);

            if (guardianElement != null) {
                guardianElement.attribute("Name_en").setValue(guardian.getName());
                guardianElement.attribute("HPBase").setValue(String.valueOf(guardian.getHpBase()));
                guardianElement.attribute("HPPer").setValue(String.valueOf(guardian.getHpPer()));
                guardianElement.attribute("GdLevel").setValue(String.valueOf(guardian.getLevel()));
                guardianElement.attribute("BaseSTR").setValue(String.valueOf(guardian.getBaseStr()));
                guardianElement.attribute("BaseSTA").setValue(String.valueOf(guardian.getBaseSta()));
                guardianElement.attribute("BaseDEX").setValue(String.valueOf(guardian.getBaseDex()));
                guardianElement.attribute("BaseWILL").setValue(String.valueOf(guardian.getBaseWill()));
                guardianElement.attribute("AddSTR").setValue(String.valueOf(guardian.getAddStr()));
                guardianElement.attribute("AddSTA").setValue(String.valueOf(guardian.getAddSta()));
                guardianElement.attribute("AddDEX").setValue(String.valueOf(guardian.getAddDex()));
                guardianElement.attribute("AddWILL").setValue(String.valueOf(guardian.getAddWill()));
                guardianElement.attribute("RewardEXP").setValue(String.valueOf(guardian.getRewardExp()));
                guardianElement.attribute("RewardGOLD").setValue(String.valueOf(guardian.getRewardGold()));
                guardianElement.attribute("BtItemID").setValue(String.valueOf(guardian.getBtItemID()));
                if (guardian.getEarth() != null && guardian.getWind() != null && guardian.getFire() != null && guardian.getWater() != null) {
                    guardianElement.attribute("Earth").setValue(guardian.getEarth() ? "1" : "0");
                    guardianElement.attribute("Wind").setValue(guardian.getWind() ? "1" : "0");
                    guardianElement.attribute("Fire").setValue(guardian.getFire() ? "1" : "0");
                    guardianElement.attribute("Water").setValue(guardian.getWater() ? "1" : "0");
                    guardianElement.attribute("ElementGrade").setValue(String.valueOf(guardian.getElementGrade()));
                }

                log.debug("Guardian updated: " + guardian.getId() + " - " + guardian.getName());
            }
        }

        try (FileWriter fileWriter = new FileWriter(filePath)) {
            OutputFormat format = OutputFormat.createPrettyPrint();
            XMLWriter writer = new XMLWriter(fileWriter, format);
            writer.write(document);
            writer.flush();
        }
    }

    private Element findProductElement(Integer productIndex, Element productListElement) {
        List<Element> productElements = productListElement.elements("Product");
        for (Element productElement : productElements) {
            if (Integer.valueOf(productElement.attributeValue("Index")).equals(productIndex)) {
                return productElement;
            }
        }
        return null;
    }

    private Element findGuardianElement(Integer guardianIndex, Element guardianListElement) {
        List<Element> guardianElements = guardianListElement.elements("Guardian");
        for (Element guardianElement : guardianElements) {
            if (Integer.valueOf(guardianElement.attributeValue("Index")).equals(guardianIndex)) {
                return guardianElement;
            }
        }
        return null;
    }
}
