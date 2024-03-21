package com.jftse.emulator.server.module;

import com.jftse.entities.database.model.item.Product;
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
import java.util.List;

@Service
@Log4j2
public class DBExporter {
    @Autowired
    private ProductRepository productRepository;

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
            updateXmlFile(productRepository.findAll(), "res/Shop_Ini3_export.xml");
        } catch (IOException e) {
            log.error("Error updating xml file", e);
        } catch (DocumentException e) {
            log.error("Error reading xml file", e);
        }
    }

    private void updateXmlFile(List<Product> products, String filePath) throws IOException, DocumentException {
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

    private Element findProductElement(Integer productIndex, Element productListElement) {
        List<Element> productElements = productListElement.elements("Product");
        for (Element productElement : productElements) {
            if (Integer.valueOf(productElement.attributeValue("Index")).equals(productIndex)) {
                return productElement;
            }
        }
        return null;
    }
}
