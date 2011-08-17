package org.gusdb.wdk.model;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.attribute.plugin.AttributePlugin;
import org.gusdb.wdk.model.attribute.plugin.AttributePluginReference;
import org.gusdb.wdk.model.query.Column;
import org.json.JSONException;

public class ColumnAttributeField extends AttributeField {

    private static final long serialVersionUID = 6599899173932240144L;
    private static Logger logger = Logger.getLogger(ColumnAttributeField.class);

    private Column column;
    private List<AttributePluginReference> pluginReferences = new ArrayList<AttributePluginReference>();
    private Map<String, AttributePlugin> plugins;

    public ColumnAttributeField() {
        super();
        // initialize the optional field values
    }

    /**
     * @return Returns the column.
     */
    public Column getColumn() {
        return this.column;
    }

    /**
     * @param column
     *                The column to set.
     * @throws WdkModelException
     */
    void setColumn(Column column) {
        this.column = column;
    }
    
    public void addAttributePluginReference(AttributePluginReference reference) {
        pluginReferences.add(reference);
    }
    
    public Map<String, AttributePlugin> getAttributePlugins() {
        return new LinkedHashMap<String, AttributePlugin>(plugins);
    }

    public void addAttributePlugin(AttributePlugin plugin) {
        plugin.setAttribute(this);
        plugins.put(plugin.getName(), plugin);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) throws WdkModelException {
        // exclude attribute plugin references
        for (int i = pluginReferences.size() - 1; i >=0; i--) {
            AttributePluginReference reference = pluginReferences.get(i);
            if (reference.include(projectId)) {
                reference.excludeResources(projectId);
            } else {
                pluginReferences.remove(i);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Field#presolveReferences(org.gusdb.wdk.model.WdkModel)
     */
    @Override
    public void resolveReferences(WdkModel wdkModel) throws WdkModelException, NoSuchAlgorithmException, WdkUserException, SQLException, JSONException {
        // verify the name
        if (!name.equals(column.getName()))
            throw new WdkModelException("The name of the ColumnAttributeField"
                    + " '" + name + "' does not match the column name '"
                    + column.getName() + "'");
        
        // resolve the attribute plugins
        plugins = new LinkedHashMap<String, AttributePlugin>();
        for(AttributePluginReference reference : pluginReferences) {
            String name = reference.getName();
            if (plugins.containsKey(name))
                throw new WdkModelException("The plugin '" + name + "' is duplicated in attribute " + this.name);
            
            reference.resolveReferences(wdkModel);
            AttributePlugin plugin = reference.getPlugin();
            plugin.setAttribute(this);
            plugins.put(name, plugin);
        }
        if (name.equals("exon_count"))
            logger.debug("plugin count: " + plugins.size());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.AttributeField#getDependents()
     */
    @Override
    public Collection<AttributeField> getDependents() {
        List<AttributeField> dependents = new ArrayList<AttributeField>();
        dependents.add(this);
        return dependents;
    }
}
