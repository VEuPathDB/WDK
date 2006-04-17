package org.gusdb.wdk.model;

public class LinkAttributeField extends AttributeField {

    private String url;
    private String visible;

    public LinkAttributeField() {
        super();
    }

    public void setVisible(String visible) {
        this.visible = visible;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    String getUrl() {
        return url;
    }

    String getVisible() {
        return visible;
    }

}
