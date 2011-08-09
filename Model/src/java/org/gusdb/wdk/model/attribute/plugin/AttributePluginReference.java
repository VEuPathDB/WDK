package org.gusdb.wdk.model.attribute.plugin;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;
import org.gusdb.wdk.model.WdkUserException;
import org.json.JSONException;

public class AttributePluginReference extends WdkModelBase {

    private String name;
    private String display;
    private String implementation;
    private String view;
    private AttributePlugin plugin;
    private List<WdkModelText> propertyList = new ArrayList<WdkModelText>();
    private Map<String, String> propertyMap;

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the display
     */
    public String getDisplay() {
        return display;
    }

    /**
     * @param display
     *            the display to set
     */
    public void setDisplay(String display) {
        this.display = display;
    }

    /**
     * @param implementation
     *            the implementation to set
     */
    public void setImplementation(String implementation) {
        this.implementation = implementation;
    }

    public void setView(String view) {
        this.view = view;
    }

    public AttributePlugin getPlugin() {
        return plugin;
    }
    
    public void addProperty(WdkModelText property) {
        this.propertyList.add(property);
    }

    @Override
    public void excludeResources(String projectId) throws WdkModelException {
        super.excludeResources(projectId);

        // exclude properties
        propertyMap = new LinkedHashMap<String, String>();
        for (WdkModelText property : propertyList) {
            if (property.include(projectId)) {
                property.excludeResources(projectId);
                String name = property.getName();
                if (propertyMap.containsKey(name))
                    throw new WdkModelException("The property '" + name
                            + "' already exists in column plugin " + this.name);
                propertyMap.put(name, property.getText());
            }
        }
        propertyList = null;
    }

    @Override
    public void resolveReferences(WdkModel wdkModel) throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        super.resolveReferences(wdkModel);

        // resolve the plugin
        try {
            Class<? extends AttributePlugin> pluginClass = Class.forName(
                    implementation).asSubclass(AttributePlugin.class);
            plugin = pluginClass.newInstance();
            plugin.setName(name);
            plugin.setDisplay(display);
            plugin.setView(view);
            plugin.setProperties(propertyMap);
        } catch (Exception ex) {
            throw new WdkModelException(ex);
        }
    }
}
