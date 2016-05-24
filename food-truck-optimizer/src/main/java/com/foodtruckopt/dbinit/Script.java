package com.foodtruckopt.dbinit;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

public class Script {

    protected String value;
    protected String type;

    @XmlValue
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @XmlAttribute(name = "Type", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    public String getType() {
        return type;
    }

    public void setType(String value) {
        this.type = value;
    }

    @Override
    public String toString() {
        return "Script [type=" + type + ", value=" + value + "]";
    }
}
