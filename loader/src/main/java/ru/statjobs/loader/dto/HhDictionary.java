package ru.statjobs.loader.dto;

public class HhDictionary {

    private final String itemGroup;
    private final String item;
    private final String code;

    public HhDictionary(String itemGroup, String item, String code) {
        this.itemGroup = itemGroup;
        this.item = item;
        this.code = code;
    }

    public String getItemGroup() {
        return itemGroup;
    }

    public String getItem() {
        return item;
    }

    public String getCode() {
        return code;
    }
}
