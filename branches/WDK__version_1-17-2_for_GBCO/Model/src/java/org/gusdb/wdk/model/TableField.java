package org.gusdb.wdk.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class TableField extends Field {

    private static final Logger logger = WdkLogManager.getLogger("org.gusdb.wdk.model.TableField");

    private String queryTwoPartName;
    private Query query;
    private List<AttributeField> attributeFieldList = new ArrayList<AttributeField>();
    private Map<String, AttributeField> attributeFieldMap = new LinkedHashMap<String, AttributeField>();

	private List<WdkModelText> descriptions = new ArrayList<WdkModelText>();
	private String description;

    Query getQuery() {
        return query;
    }

    void setQuery(Query query) {
        this.query = query;
    }

    public void setQueryRef(String queryRef) {
        this.queryTwoPartName = queryRef;
    }

    public String getQueryRef() {
        return queryTwoPartName;
    }

	public void addDescription(WdkModelText description) {
		this.descriptions.add(description);
	}

	public String getDescription() {
		return (description == null) ? "" : description;
	}

    public void addAttributeField(AttributeField attributeField) {
        attributeFieldList.add(attributeField);
    }

    public AttributeField[] getAttributeFields() {
        AttributeField[] array = new AttributeField[attributeFieldMap.size()];
        attributeFieldMap.values().toArray(array);
        return array;
    }

    public Map<String, AttributeField> getAttributeFieldMap() {
        return new LinkedHashMap<String, AttributeField>(attributeFieldMap);
    }

    public AttributeField[] getReportMakerFields() {
        List<AttributeField> fields = new ArrayList<AttributeField>();
        for (AttributeField field : attributeFieldMap.values()) {
            if (field.getInReportMaker()) fields.add(field);
        }
        AttributeField[] array = new AttributeField[fields.size()];
        fields.toArray(array);
        return array;
    }

    public AttributeField[] getDisplayableFields() {
        List<AttributeField> fields = new ArrayList<AttributeField>();
        for (AttributeField field : attributeFieldMap.values()) {
            if (!field.getInternal()) fields.add(field);
        }
        AttributeField[] array = new AttributeField[fields.size()];
        fields.toArray(array);
        return array;
    }

    /*
     * (non-Javadoc) Should never be called, but is necessary because TableField
     * implements FieldI.
     * 
     * @see org.gusdb.wdk.model.FieldI#getTruncateTo()
     */
    public int getTruncateTo() {
        throw new RuntimeException("getTruncate does not apply to TableField");
    }

    void resolveReferences(WdkModel model) throws WdkModelException {
        // resolve Query
        query = (Query) model.resolveReference(queryTwoPartName);
        Column[] columns = query.getColumns();
        for (Column column : columns) {
            AttributeField field = attributeFieldMap.get(column.getName());
            if (field != null && field instanceof ColumnAttributeField) {
                ((ColumnAttributeField) field).setColumn(column);
            } else {
                String message = "The Column of name '" + column.getName()
                        + "' doesn't match with any ColumnAttributeField in"
                        + " TableField " + getName() + ".";
                logger.finest(message);
                throw new WdkModelException(message);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) throws WdkModelException {
		// exclude descriptions
		boolean hasDescription = false;
		for (WdkModelText description : descriptions) {
			if (description.include(projectId)) {
				if (hasDescription) {
					throw new WdkModelException("The table field " + name
							+ " of recordClass " + recordClass.getFullName()
							+ " has more than one description for project "
							+ projectId);
				} else {
					this.description = description.getText();
					hasDescription = true;
				}
			}
		}
		descriptions = null;

        // exclude attributes
        for (AttributeField field : attributeFieldList) {
            if (field.include(projectId)) {
                field.excludeResources(projectId);
                String fieldName = field.getName();
                if (attributeFieldMap.containsKey(fieldName))
                    throw new WdkModelException("The attributeField "
                            + fieldName + " is duplicated in table "
                            + this.name);
                attributeFieldMap.put(fieldName, field);
            }
        }
        attributeFieldList = null;
    }
}
