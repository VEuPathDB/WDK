package org.gusdb.wdk.model;


public class TextAttribute {

    String name;
    String displayName;
    String text;

    public TextAttribute() {}

    public void setName(String name) {
	this.name = name;
    }
    
    public String getName() {
	return name;
    }
   
    public void setDisplayName(String displayName) {
	this.displayName = displayName;
    }
    
    public String getDisplayName() {
	return (displayName != null)? displayName : name;
    }
    
     public void setText(String text) {
	this.text = text;
    }

    public String getText() {
	return text;
    }
    
}
