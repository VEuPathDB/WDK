package org.gusdb.wdk.model.record.attribute.plugin;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;
import org.gusdb.wdk.model.record.attribute.AttributeField;

/**
 * @author jerric
 * 
 *         the reference is stored in the model, thus in application-scope.
 */
public class AttributePluginReference extends WdkModelBase {

    private String name;
    private String display;
    private String description;
    private String implementation;
    private String view;
    private AttributeField attributeField;
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
     * @return the description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * @param description
     *            the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @param implementation
     *            the implementation to set
     */
    public void setImplementation(String implementation) {
        this.implementation = implementation;
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    public void setAttributeField(AttributeField attributeField) {
        this.attributeField = attributeField;
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
    public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
        super.resolveReferences(wdkModel);

        // make sure the implementation does implement AttributePlugin interface
        try {
            Class<?> pluginClass = Class.forName(implementation);
            Object plugin = pluginClass.newInstance();
            if (!(plugin instanceof AttributePlugin))
                throw new WdkModelException("The implementation '"
                        + implementation + "' of attribute plugin '" + name
                        + "' must implement interface "
                        + AttributePlugin.class.getCanonicalName());
        }
        catch (Exception ex) {
            throw new WdkModelException(ex);
        }
    }

    public AttributePlugin getPlugin() throws InstantiationException,
            IllegalAccessException, ClassNotFoundException {
        Class<? extends AttributePlugin> pluginClass = Class.forName(
                implementation).asSubclass(AttributePlugin.class);
        AttributePlugin plugin = pluginClass.newInstance();
        plugin.setName(name);
        plugin.setDisplay(display);
        plugin.setDescription(description);
        plugin.setView(view);
        plugin.setProperties(propertyMap);
        plugin.setAttributeField(attributeField);

        return plugin;
    }
}
