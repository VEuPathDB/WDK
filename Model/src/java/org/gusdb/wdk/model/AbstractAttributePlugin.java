package org.gusdb.wdk.model;

import java.util.Map;

import org.gusdb.wdk.model.attribute.plugin.AttributePlugin;

public abstract class AbstractAttributePlugin implements AttributePlugin {

    private String name;
    private String display;
    private String view;
    protected Map<String, String> properties;
    protected ColumnAttributeField attribute;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDisplay() {
        return (display == null) ? name : display;
    }

    @Override
    public void setDisplay(String display) {
        this.display = display;
    }

    @Override
    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    @Override
    public void setAttribute(ColumnAttributeField attribute) {
        this.attribute = attribute;
    }

    /**
     * @return the view
     */
    public String getView() {
        return view;
    }

    /**
     * @param view
     *            the view to set
     */
    public void setView(String view) {
        this.view = view;
    }
}
