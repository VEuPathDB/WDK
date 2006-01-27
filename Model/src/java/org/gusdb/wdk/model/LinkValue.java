package org.gusdb.wdk.model;

/**
 * A value representing a hyperlink
 */
public class LinkValue {

    String visible;
    String url;
    LinkAttributeField field;

    public LinkValue(String visible, String url, LinkAttributeField field) {
	this.visible = visible;
	this.url = url;
	this.field = field;
    }

    public String getVisible() {
	return visible;
    }

    public boolean getInternal() {
	return field.getInternal();
    }

    public String getUrl() {
	return url;
    }

    public String toString() {
	return visible + "(" + url + ")";
    }
}
