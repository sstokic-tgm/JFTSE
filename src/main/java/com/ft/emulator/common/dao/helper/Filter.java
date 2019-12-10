package com.ft.emulator.common.dao.helper;

public class Filter {

    /**
     * The property eg. the field to scan with the filters value.
     */
    private String property;
    /**
     * The value to compare with. You might add a leading and trailing % when using the LIKE operant
     */
    private Object value;
    /**
     * The compare operant eg. "=" for Objects or "LIKE" for Strings
     */
    private String compareOperant;

    public Filter(String property, Object value, String compareOparant) {

        this.property = property;
        this.value = value;
        this.compareOperant = compareOparant;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getCompareOparant() {
        return compareOperant;
    }

    public void setCompareOparant(String compareOparant) {
        this.compareOperant = compareOparant;
    }
}