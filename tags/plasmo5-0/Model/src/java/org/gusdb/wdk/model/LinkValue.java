package org.gusdb.wdk.model;

/**
 * A value representing a hyperlink
 */
public class LinkValue {

    String visible;
    String url;

    public LinkValue(String visible, String url) {
	this.visible = visible;
	this.url = url;
    }

    public String getVisible() {
	return visible;
    }

    public String getUrl() {
	return url;
    }

    public String toString() {
	return visible + "(" + url + ")";
    }
}
