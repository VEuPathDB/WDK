package org.gusdb.wdk.model;

public class TextAttributeField extends AttributeField {

    String text;

    public TextAttributeField() {
        super();
    }

    public void setText(String text) {
        this.text = text;
    }

    String getText() {
        return text;
    }
}
