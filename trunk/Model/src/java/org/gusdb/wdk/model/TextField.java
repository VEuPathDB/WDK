package org.gusdb.gus.wdk.model;


public class TextField {

    String name;
    String text;

    public TextField() {}

    public void setName(String name) {
	this.name = name;
    }
    
    public String getName() {
	return name;
    }
    
    public void setText(String text) {
	this.text = text;
    }

    public String getText() {
	return text;
    }
    
}
