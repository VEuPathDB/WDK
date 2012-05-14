/**
 * 
 */
package org.gusdb.wdk.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.attribute.plugin.AttributePluginReference;

/**
 * @author Jerric
 * @created Jan 19, 2006
 */
public abstract class AttributeField extends Field {

    public static final Pattern MACRO_PATTERN = Pattern.compile(
            "\\$\\$([^\\$]+?)\\$\\$", Pattern.MULTILINE);

    private static Logger logger = Logger.getLogger(AttributeField.class);

    protected abstract Collection<AttributeField> getDependents()
            throws WdkModelException;

    protected AttributeFieldContainer container;

    private boolean sortable = true;
    private String align;
    private boolean nowrap = false;
    private boolean removable = true;
    private String categoryName;

    private List<AttributePluginReference> pluginList = new ArrayList<AttributePluginReference>();
    private Map<String, AttributePluginReference> pluginMap;

    /**
     * by default, an attribute can be removed from the result page.
     * 
     * @return
     */
    public boolean isRemovable() {
        return removable;
    }

    /**
     * @param removable
     *            the removable to set
     */
    public void setRemovable(boolean removable) {
        this.removable = removable;
    }

    /**
     * @return the sortable
     */
    public boolean isSortable() {
        return sortable;
    }

    /**
     * @param sortable
     *            the sortable to set
     */
    public void setSortable(boolean sortable) {
        this.sortable = sortable;
    }

    /**
     * @return the align
     */
    public String getAlign() {
        return align;
    }

    /**
     * @param align
     *            the align to set
     */
    public void setAlign(String align) {
        this.align = align;
    }

    /**
     * @return the nowrap
     */
    public boolean isNowrap() {
        return nowrap;
    }

    /**
     * @param nowrap
     *            the nowrap to set
     */
    public void setNowrap(boolean nowrap) {
        this.nowrap = nowrap;
    }

    /**
     * @return attribute category name
     */
    public String getAttributeCategory() {
        return categoryName;
    }

    /**
     * @param categoryName
     *            attribute category name
     */
    public void setAttributeCategory(String categoryName) {
        this.categoryName = categoryName;
    }

    /**
     * @param container
     *            the container to set
     */
    public void setContainer(AttributeFieldContainer container) {
        this.container = container;
    }

    public void addAttributePluginReference(AttributePluginReference reference) {
        reference.setAttributeField(this);
        if (pluginList != null) pluginList.add(reference);
        else pluginMap.put(reference.getName(), reference);
    }

    public Map<String, AttributePluginReference> getAttributePlugins() {
        return new LinkedHashMap<String, AttributePluginReference>(pluginMap);
    }

    public Map<String, ColumnAttributeField> getColumnAttributeFields() throws WdkModelException {
        Map<String, ColumnAttributeField> leaves = new LinkedHashMap<String, ColumnAttributeField>();
        for (AttributeField dependent : getDependents()) {
            if (dependent instanceof ColumnAttributeField) {
                leaves.put(dependent.getName(),
                        (ColumnAttributeField) dependent);
            } else {
                leaves.putAll(dependent.getColumnAttributeFields());
            }
        }
        return leaves;
    }

    protected Map<String, AttributeField> parseFields(String text)
            throws WdkModelException {
        Map<String, AttributeField> children = new LinkedHashMap<String, AttributeField>();
        Map<String, AttributeField> fields = container.getAttributeFieldMap();

        Matcher matcher = AttributeField.MACRO_PATTERN.matcher(text);
        while (matcher.find()) {
            String fieldName = matcher.group(1);
            if (!fields.containsKey(fieldName)) {
                logger.warn("Invalid field macro in attribute" + " [" + name
                        + "] of [" + recordClass.getFullName() + "]: "
                        + fieldName);
                continue;
            }

            AttributeField field = fields.get(fieldName);
            children.put(fieldName, field);
            if (!children.containsKey(fieldName))
                children.put(fieldName, field);
        }
        return children;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) throws WdkModelException {
        super.excludeResources(projectId);

        // exclude attribute plugin references
        pluginMap = new LinkedHashMap<String, AttributePluginReference>();
        for (AttributePluginReference plugin : pluginList) {
            if (plugin.include(projectId)) {
                String name = plugin.getName();
                if (pluginMap.containsKey(name))
                    throw new WdkModelException("The plugin '" + name
                            + "' is duplicated in attribute " + this.name);
                plugin.excludeResources(projectId);
                pluginMap.put(name, plugin);
            }
        }
        pluginList = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.Field#presolveReferences(org.gusdb.wdk.model.WdkModel
     * )
     */
    @Override
    public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
        super.resolveReferences(wdkModel);

        // resolve plugin references
        for (AttributePluginReference plugin : pluginMap.values()) {
            plugin.resolveReferences(wdkModel);
        }

        // check dependency loops
        traverseDependeny(this, new Stack<String>());
    }

    /**
     * 
     * @param attribute
     *            the attribute to be checked
     * @param path
     *            the path from root to the attribute (attribute is not included
     * @throws WdkModelException
     */
    private void traverseDependeny(AttributeField attribute, Stack<String> path)
            throws WdkModelException {
        if (path.contains(attribute.name))
            throw new WdkModelException("Attribute has loop reference: "
                    + attribute.name);

        path.push(attribute.name);
        for (AttributeField dependent : attribute.getDependents()) {
            traverseDependeny(dependent, path);
        }
        path.pop();
    }
}
