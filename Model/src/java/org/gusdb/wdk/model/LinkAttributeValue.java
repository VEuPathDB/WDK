package org.gusdb.wdk.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A value representing a hyperlink
 */
public class LinkAttributeValue extends AttributeValue {

    private AttributeValueContainer container;
    private LinkAttributeField field;
    private String displayText;
    private String url;

    public LinkAttributeValue(LinkAttributeField field,
            AttributeValueContainer container) {
        super(field);
        this.field = field;
        this.container = container;
    }

    public String getDisplayText() throws WdkModelException, WdkUserException {
        if (displayText == null) {
            String text = field.getDisplayText();
            Map<String, AttributeField> subFields = field.parseFields(text);
            Map<String, Object> values = new LinkedHashMap<String, Object>();
            for (String subField : subFields.keySet()) {
                AttributeValue value = container.getAttributeValue(subField);
                values.put(subField, value.getValue());
            }
            this.displayText = Utilities.replaceMacros(text, values);
        }
        return this.displayText;
    }

    public String getUrl() throws WdkModelException, WdkUserException {
        if (this.url == null) {
            String url = field.getUrl();
            Map<String, AttributeField> subFields = field.parseFields(url);
            Map<String, Object> values = new LinkedHashMap<String, Object>();
            for (String subField : subFields.keySet()) {
                AttributeValue value = container.getAttributeValue(subField);
                values.put(subField, value.getValue());
            }
            this.url = Utilities.replaceMacros(url, values);
        }
        return this.url;
    }

    @Override
    public Object getValue() throws WdkModelException, WdkUserException {
        return getDisplayText() + "(" + getUrl() + ")";
    }
}
