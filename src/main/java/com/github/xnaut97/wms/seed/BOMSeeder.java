package com.github.xnaut97.wms.seed;

import com.github.xnaut97.wms.entity.bom.BOM;
import com.github.xnaut97.wms.entity.bom.BOMItem;
import com.github.xnaut97.wms.entity.material.Material;
import com.github.xnaut97.wms.entity.product.Product;
import com.github.xnaut97.wms.repository.MaterialRepository;
import com.github.xnaut97.wms.repository.product.ProductRepository;
import com.github.xnaut97.wms.repository.bom.BOMRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class BOMSeeder {

    private final BOMRepository bomRepository;

    private final ProductRepository productRepository;

    private final MaterialRepository materialRepository;


    @Transactional
    public void seed() {

        if (productRepository.count() == 0) {
            return;
        }

        if (materialRepository.count() == 0) {
            return;
        }

        if (bomRepository.count() > 0) {
            return;
        }


        // =========================================================
        // PRODUCTS
        // =========================================================

        Product steelCabinet =
                productRepository.findByCode("FP001")
                        .orElseThrow();

        Product officeDesk =
                productRepository.findByCode("FP002")
                        .orElseThrow();

        Product metalShelf =
                productRepository.findByCode("FP003")
                        .orElseThrow();

        Product toolBox =
                productRepository.findByCode("FP004")
                        .orElseThrow();

        Product electricalPanel =
                productRepository.findByCode("FP005")
                        .orElseThrow();


        // =========================================================
        // MATERIALS
        // =========================================================

        Material steel =
                materialRepository.findByCode("RM001")
                        .orElseThrow();

        Material copperWire =
                materialRepository.findByCode("RM002")
                        .orElseThrow();

        Material absPlastic =
                materialRepository.findByCode("RM003")
                        .orElseThrow();

        Material aluminum =
                materialRepository.findByCode("RM004")
                        .orElseThrow();

        Material hydraulicOil =
                materialRepository.findByCode("RM005")
                        .orElseThrow();

        Material lubricant =
                materialRepository.findByCode("RM006")
                        .orElseThrow();

        Material pvcPipe =
                materialRepository.findByCode("RM007")
                        .orElseThrow();

        Material bearing =
                materialRepository.findByCode("RM008")
                        .orElseThrow();

        Material controlPcb =
                materialRepository.findByCode("RM009")
                        .orElseThrow();

        Material hexBolt =
                materialRepository.findByCode("RM010")
                        .orElseThrow();


        // =========================================================
        // BOM 001 - Steel Cabinet
        // =========================================================

        BOM bom001 = new BOM();

        bom001.setCode("BOM001");
        bom001.setProduct(steelCabinet);
        bom001.setEnabled(true);
        bom001.setItems(new ArrayList<>());

        bom001.getItems().add(
                createItem(
                        bom001,
                        steel,
                        "25.00",
                        "70.00",
                        "2.00"
                )
        );

        bom001.getItems().add(
                createItem(
                        bom001,
                        aluminum,
                        "8.00",
                        "20.00",
                        "3.00"
                )
        );

        bom001.getItems().add(
                createItem(
                        bom001,
                        hexBolt,
                        "24.00",
                        "10.00",
                        "1.00"
                )
        );

        bomRepository.save(bom001);


        // =========================================================
        // BOM 002 - Office Desk
        // =========================================================

        BOM bom002 = new BOM();

        bom002.setCode("BOM002");
        bom002.setProduct(officeDesk);
        bom002.setEnabled(true);
        bom002.setItems(new ArrayList<>());

        bom002.getItems().add(
                createItem(
                        bom002,
                        steel,
                        "15.00",
                        "55.00",
                        "2.00"
                )
        );

        bom002.getItems().add(
                createItem(
                        bom002,
                        aluminum,
                        "6.00",
                        "25.00",
                        "3.00"
                )
        );

        bom002.getItems().add(
                createItem(
                        bom002,
                        absPlastic,
                        "4.00",
                        "10.00",
                        "2.00"
                )
        );

        bom002.getItems().add(
                createItem(
                        bom002,
                        hexBolt,
                        "16.00",
                        "10.00",
                        "1.00"
                )
        );

        bomRepository.save(bom002);


        // =========================================================
        // BOM 003 - Metal Shelf
        // =========================================================

        BOM bom003 = new BOM();

        bom003.setCode("BOM003");
        bom003.setProduct(metalShelf);
        bom003.setEnabled(true);
        bom003.setItems(new ArrayList<>());

        bom003.getItems().add(
                createItem(
                        bom003,
                        steel,
                        "30.00",
                        "65.00",
                        "2.00"
                )
        );

        bom003.getItems().add(
                createItem(
                        bom003,
                        aluminum,
                        "7.00",
                        "15.00",
                        "3.00"
                )
        );

        bom003.getItems().add(
                createItem(
                        bom003,
                        bearing,
                        "12.00",
                        "10.00",
                        "2.00"
                )
        );

        bom003.getItems().add(
                createItem(
                        bom003,
                        hexBolt,
                        "30.00",
                        "10.00",
                        "1.00"
                )
        );

        bomRepository.save(bom003);


        // =========================================================
        // BOM 004 - Tool Box
        // =========================================================

        BOM bom004 = new BOM();

        bom004.setCode("BOM004");
        bom004.setProduct(toolBox);
        bom004.setEnabled(true);
        bom004.setItems(new ArrayList<>());

        bom004.getItems().add(
                createItem(
                        bom004,
                        absPlastic,
                        "8.00",
                        "45.00",
                        "2.00"
                )
        );

        bom004.getItems().add(
                createItem(
                        bom004,
                        aluminum,
                        "5.00",
                        "25.00",
                        "3.00"
                )
        );

        bom004.getItems().add(
                createItem(
                        bom004,
                        bearing,
                        "4.00",
                        "15.00",
                        "2.00"
                )
        );

        bom004.getItems().add(
                createItem(
                        bom004,
                        lubricant,
                        "0.50",
                        "10.00",
                        "1.00"
                )
        );

        bom004.getItems().add(
                createItem(
                        bom004,
                        hydraulicOil,
                        "0.20",
                        "5.00",
                        "2.00"
                )
        );

        bomRepository.save(bom004);


        // =========================================================
        // BOM 005 - Electrical Panel
        // =========================================================

        BOM bom005 = new BOM();

        bom005.setCode("BOM005");
        bom005.setProduct(electricalPanel);
        bom005.setEnabled(true);
        bom005.setItems(new ArrayList<>());

        bom005.getItems().add(
                createItem(
                        bom005,
                        steel,
                        "12.00",
                        "35.00",
                        "2.00"
                )
        );

        bom005.getItems().add(
                createItem(
                        bom005,
                        absPlastic,
                        "4.00",
                        "15.00",
                        "2.00"
                )
        );

        bom005.getItems().add(
                createItem(
                        bom005,
                        copperWire,
                        "25.00",
                        "25.00",
                        "2.00"
                )
        );

        bom005.getItems().add(
                createItem(
                        bom005,
                        controlPcb,
                        "1.00",
                        "15.00",
                        "1.00"
                )
        );

        bom005.getItems().add(
                createItem(
                        bom005,
                        pvcPipe,
                        "3.00",
                        "10.00",
                        "3.00"
                )
        );

        bomRepository.save(bom005);


        System.out.println("✓ BOMs seeded");
    }


    private BOMItem createItem(
            BOM bom,
            Material material,
            String consumptionQuantity,
            String mixingRatio,
            String maxWasteRatio
    ) {

        BOMItem item = new BOMItem();

        item.setBom(bom);
        item.setMaterial(material);

        item.setConsumptionQuantity(
                new BigDecimal(consumptionQuantity)
        );

        item.setMixingRatio(
                new BigDecimal(mixingRatio)
        );

        item.setMaxWasteRatio(
                new BigDecimal(maxWasteRatio)
        );

        return item;
    }

}