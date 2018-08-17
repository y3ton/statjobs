package ru.statjobs.loader.common.dto;

import java.io.Serializable;

public class HhDictionary implements Serializable {

    private final String group;
    private final String groupCode;
    private final String item;
    private final String itemCode;

    public HhDictionary(String group, String groupCode, String item, String code) {
        this.group = group;
        this.groupCode = groupCode;
        this.item = item;
        this.itemCode = code;
    }

    public String getGroup() {
        return group;
    }

    public String getGroupCode() {
        return groupCode;
    }

    public String getItem() {
        return item;
    }

    public String getItemCode() {
        return itemCode;
    }
}
