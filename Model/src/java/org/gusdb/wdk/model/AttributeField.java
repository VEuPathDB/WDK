/**
 * 
 */
package org.gusdb.wdk.model;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Jerric
 * @created Jan 19, 2006
 */
public abstract class AttributeField extends Field {

    public abstract Collection<ColumnAttributeField> getDependents()
            throws WdkModelException;

    protected AttributeFieldContainer container;

    private boolean sortable = true;
    private String align;
    private boolean nowrap = false;

    /**
     * @return the sortable
     */
    public boolean isSortable() {
        return sortable;
    }

    /**
     * @param sortable
     *                the sortable to set
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
     *                the align to set
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
     *                the nowrap to set
     */
    public void setNowrap(boolean nowrap) {
        this.nowrap = nowrap;
    }

    /**
     * @param container
     *                the container to set
     */
    public void setContainer(AttributeFieldContainer container) {
        this.container = container;
    }

    protected Map<String, ColumnAttributeField> parseFields(String text)
            throws WdkModelException {
        Map<String, ColumnAttributeField> children = new LinkedHashMap<String, ColumnAttributeField>();
        Map<String, AttributeField> fields = container.getAttributeFieldMap();

        String type = ColumnAttributeField.class.getName();
        Pattern pattern = Pattern.compile("\\$\\$(.+?)\\$\\$", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String fieldName = matcher.group(1);
            if (!fields.containsKey(fieldName)) continue;
            AttributeField field = fields.get(fieldName);
            if (!(field instanceof ColumnAttributeField))
                throw new WdkModelException("Only " + type + " can "
                        + "be embedded into the text content. " + fieldName
                        + " is not a " + type + ".");
            if (!children.containsKey(fieldName))
                children.put(fieldName, (ColumnAttributeField) field);
        }
        return children;
    }
}
