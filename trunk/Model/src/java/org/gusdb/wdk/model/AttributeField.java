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

    public abstract Collection<AttributeField> getDependents()
            throws WdkModelException;

    protected AttributeFieldContainer container;

    private boolean sortable = true;
    private String align;
    private boolean nowrap = false;
    private boolean removable = true;

    /**
     * by default, an attribute can be removed from the result page.
     * @return
     */
    public boolean isRemovable() {
        return removable;
    }
    

    /**
     * @param removable the removable to set
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
     * @param container
     *            the container to set
     */
    public void setContainer(AttributeFieldContainer container) {
        this.container = container;
    }

    protected Map<String, AttributeField> parseFields(String text)
            throws WdkModelException {
        Map<String, AttributeField> children = new LinkedHashMap<String, AttributeField>();
        Map<String, AttributeField> fields = container.getAttributeFieldMap();

        Pattern pattern = Pattern.compile("\\$\\$(.+?)\\$\\$", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String fieldName = matcher.group(1);
            if (!fields.containsKey(fieldName)) continue;
            AttributeField field = fields.get(fieldName);
            if (!(field instanceof ColumnAttributeField)
                    && !(field instanceof PrimaryKeyAttributeField))
                throw new WdkModelException("Only columnAttribute or "
                        + "primaryKeyAttribute can be embedded into the text "
                        + "content. " + fieldName + " is of type "
                        + field.getClass().getSimpleName());
            if (!children.containsKey(fieldName))
                children.put(fieldName, field);
        }
        return children;
    }
}
