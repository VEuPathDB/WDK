package org.gusdb.wdk.model.record.attribute;

import java.util.ArrayList;
import java.util.List;

import org.gusdb.wdk.model.TreeNode;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.record.FieldScope;
import org.gusdb.wdk.model.record.TableField;

/**
 * A tree structure used to organize the {@link AttributeField}s and {@link TableField}s in trees.
 * 
 * @author Ryan
 *
 */
public class AttributeCategory extends WdkModelBase {

    private static String newline = System.getProperty("line.separator");

	private String name;
	private String displayName;
	private List<AttributeCategory> subcategories = new ArrayList<AttributeCategory>();
	private List<AttributeField> fields = new ArrayList<AttributeField>();
	
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
		return displayName;
	}
	
	public void addAttributeCategory(AttributeCategory category) {
		subcategories.add(category);
	}
	public List<AttributeCategory> getSubCategories() {
		return subcategories;
	}

	public void addField(AttributeField field) {
		fields.add(field);
	}
	public void setFields(List<AttributeField> newFields) {
		fields = newFields;
	}
	public List<AttributeField> getFields() {
		return fields;
	}
	
	/**
	 * Creates copy of this object and returns it.  Deeply copies
	 * nested AttributeCategories, but only copies references to
	 * AttributeFields (List of fields is a copy as though).  Trims
	 * out subcategories that do not contain any fields.
	 * 
	 * @return copy of this AttribueCategory
	 */
	public AttributeCategory getTrimmedCopy(FieldScope scope) {
		AttributeCategory copy = new AttributeCategory();
		copy.name = name;
		copy.displayName = displayName;
		for (AttributeCategory cat : subcategories) {
			AttributeCategory catCopy = cat.getTrimmedCopy(scope);
			if (!(catCopy.subcategories.isEmpty() && catCopy.fields.isEmpty())) {
				copy.subcategories.add(catCopy);
			}
		}
		for (AttributeField field : fields) {
			if (scope.isFieldInScope(field)) {
				copy.fields.add(field);
			}
		}
		return copy;
	}
	
	public void appendToStringBuffer(String indentation, StringBuilder builder) {
		builder.append(indentation).append(name).append(" (").append(fields.size()).append(")").append(newline);
		for (AttributeCategory cat : subcategories) {
			cat.appendToStringBuffer(indentation + "  ", builder);
		}
	}
	public TreeNode toTreeNode() {
		TreeNode node = new TreeNode(getName(), getDisplayName());
		for (AttributeCategory cat : subcategories) {
			node.addChildNode(cat.toTreeNode());
		}
		for (AttributeField attrib : fields) {
			node.addChildNode(new TreeNode(attrib.getName(), attrib.getDisplayName(), attrib.getHelp()));
		}
		return node;
	}
}
