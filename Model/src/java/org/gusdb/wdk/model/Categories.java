package org.gusdb.wdk.model;

import java.util.ArrayList;
import java.util.List;


public class Categories extends WdkModelBase {

	private String recordClassRef;
	private RecordClass recordClass;
	
	private List<Category> categories;
	
	public Categories() {
	    categories = new ArrayList<Category>();
	}
	
	public RecordClass getRecordClass() {
		return recordClass;
	}


	public void setRecordClassRef(String recordClassRef) {
		this.recordClassRef = recordClassRef;
	}
	
	public void addCategory(Category category) {
	    category.setCategories(this);
	    categories.add(category);
	}

	public Category[] getCategories() {
	    Category[] array = new Category[categories.size()];
	    categories.toArray(array);
	    return array;
	}

	@Override
	public void excludeResources(String projectId) throws WdkModelException {
		// exclude categories
	    List<Category> temp = new ArrayList<Category>();
	    for(Category category : categories) {
	        if (category.include(projectId)) {
	            category.excludeResources(projectId);
	            temp.add(category);
	        }
	    }
	    categories.clear();
	    categories = temp;
	}


	public void resolveReferences(WdkModel model) throws WdkModelException {
		// resolve the base recordClass
	    recordClass = (RecordClass)model.resolveReference(recordClassRef);
	    
	    // resolve the references in category
	    for(Category category : categories) {
	        category.resolveReferences(model);
	    }
	}
}
