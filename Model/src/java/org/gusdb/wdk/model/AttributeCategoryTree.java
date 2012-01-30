package org.gusdb.wdk.model;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;

public class AttributeCategoryTree extends WdkModelBase {
	
    private static String newline = System.getProperty("line.separator");
    
	private List<AttributeCategory> topLevelCategories = new ArrayList<AttributeCategory>();
	private List<AttributeField> topLevelAttributes = new ArrayList<AttributeField>();
	
	// map from category name to Category (for easy lookup when placing attributes)
	private Map<String, AttributeCategory> categoryMap = new HashMap<String, AttributeCategory>();
	
	public void addAttributeCategory(AttributeCategory category) {
		topLevelCategories.add(category);
	}

	public void prependAttributeCategory(AttributeCategory dynamic) {
		topLevelCategories.add(0, dynamic);
	}
	
	public void addAttributeToCategories(AttributeField attribute) throws WdkModelException {
		if (attribute.getAttributeCategory() == null) {
			// add no-category attribute to default list
			topLevelAttributes.add(attribute);
			return;
		}
		// split attributeCategory into individual names
		String[] categories = attribute.getAttributeCategory().split(",");
		for (String category : categories) {
			category = category.trim();
			if (!categoryMap.containsKey(category)) {
				throw new WdkModelException("Attribute field " + attribute.getName() +
						" is assigned to a category '" + category + "' that does not exist.");
			}
			categoryMap.get(category).addField(attribute);
		}
	}

	/**
	 * Creates and returns a copy of this tree with only attributes valid under
	 * the given scope.  Also recursively trims categories that do not have attributes.
	 * 
	 * @param scope scope in which attributes must be valid
	 * @return copy of tree, trimmed for scope and empty categories
	 */
	public AttributeCategoryTree getTrimmedCopy(FieldScope scope) {
		AttributeCategoryTree copy = new AttributeCategoryTree();
		for (AttributeCategory cat : topLevelCategories) {
			AttributeCategory copyCat = cat.getTrimmedCopy(scope);
			if (!(copyCat.getFields().isEmpty() && copyCat.getSubCategories().isEmpty())) {
				copy.topLevelCategories.add(copyCat);
			}
		}
		for (AttributeField field : topLevelAttributes) {
			if (scope.isFieldInScope(field)) {
				copy.topLevelAttributes.add(field);
			}
		}
		try {
			copy.resolveReferences(getWdkModel());
		}
		catch (WdkModelException e) {
			throw new IllegalStateException("Existing category tree has been corrupted.", e);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("Existing category tree has been corrupted.", e);
		} catch (WdkUserException e) {
			throw new IllegalStateException("Existing category tree has been corrupted.", e);
		} catch (SQLException e) {
			throw new IllegalStateException("Existing category tree has been corrupted.", e);
		} catch (JSONException e) {
			throw new IllegalStateException("Existing category tree has been corrupted.", e);
		}
		return copy;
	}
	
	public List<AttributeCategory> getTopLevelCategories() {
		return topLevelCategories;
	}
	
	/**
	 * Builds out a map from category name to category
	 * @throws WdkModelException 
	 * @throws JSONException 
	 * @throws SQLException 
	 * @throws WdkUserException 
	 * @throws NoSuchAlgorithmException 
	 */
	@Override
	public void resolveReferences(WdkModel model) throws WdkModelException,
			NoSuchAlgorithmException, WdkUserException, SQLException, JSONException {
		super.resolveReferences(model);
		for (AttributeCategory cat : topLevelCategories) {
			addCategoryToMap(cat);
		}
	}

	/**
	 * Recursively adds the passed category and all subcategories to the category map
	 * 
	 * @param cat category to add
	 * @throws WdkModelException if any categories' names are duplicated
	 */
	private void addCategoryToMap(AttributeCategory cat) throws WdkModelException {
		if (categoryMap.containsKey(cat.getName())) {
			throw new WdkModelException("Attribute categories must be unique. " +
					cat.getName() + " is duplicated.");
		}
		categoryMap.put(cat.getName(), cat);
		for (AttributeCategory subcat : cat.getSubCategories()) {
			addCategoryToMap(subcat);
		}
	}
	
	public String toString() {
		StringBuilder str = new StringBuilder()
			.append("uncategorized (").append(topLevelAttributes.size()).append(")").append(newline);
		for (AttributeCategory cat : topLevelCategories) {
			cat.appendToStringBuffer("", str);
		}
		return str.toString();
	}

	public TreeNode toTreeNode(String rootName, String rootDisplayName) {
		TreeNode root = new TreeNode(rootName, rootDisplayName);
		for (AttributeCategory cat : topLevelCategories) {
			root.addChildNode(cat.toTreeNode());
		}
		for (AttributeField attrib : topLevelAttributes) {
			root.addChildNode(new TreeNode(attrib.getName(), attrib.getDisplayName(), attrib.getHelp()));
		}
		return root;
	}
}
