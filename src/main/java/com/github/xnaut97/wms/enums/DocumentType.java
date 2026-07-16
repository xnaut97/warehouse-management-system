package com.github.xnaut97.wms.enums;

import lombok.Getter;

@Getter
public enum DocumentType {

    GOODS_RECEIPT("GR"),

    GOODS_ISSUE("GI"),

    INVENTORY_CHECK("IC"),

    INVENTORY_ADJUSTMENT("ADJ");

    private final String prefix;

    DocumentType(String prefix) {
        this.prefix = prefix;
    }

}