package org.gusdb.wdk.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Categories extends WdkModelBase {

    private String recordClassRef;
    private RecordClass recordClass;

    private List<Category> categoryList;
    private Map<String, Category> categoryMap;

    public Categories() {
        categoryList = new ArrayList<Category>();
        categoryMap = new LinkedHashMap<String, Category>();
    }

    public String getRecordClassRef() {
        return recordClassRef;
    }

    public RecordClass getRecordClass() {
        return recordClass;
    }

    public void setRecordClassRef(String recordClassRef) {
        this.recordClassRef = recordClassRef;
    }

    public void addCategory(Category category) {
        category.setCategories(this);
        categoryList.add(category);
    }

    public Category[] getCategories() {
        Category[] array = new Category[categoryMap.size()];
        categoryMap.values().toArray(array);
        return array;
    }

    @Override
    public void excludeResources(String projectId) throws WdkModelException {
        // exclude categories
        for (Category category : categoryList) {
            if (category.include(projectId)) {
                String name = category.getName();
                if (categoryMap.containsKey(name))
                    throw new WdkModelException("Category '" + name
                            + "' is duplicated.");
                category.excludeResources(projectId);
                categoryMap.put(name, category);
            }
        }
        categoryList = null;
    }

    public void resolveReferences(WdkModel model) throws WdkModelException {
        // resolve the base recordClass
        recordClass = (RecordClass) model.resolveReference(recordClassRef);

        List<String> removeList = new ArrayList<String>();
        
        // resolve the references in category
        for (Category category : categoryMap.values()) {
            category.resolveReferences(model);
            // remove the empty categories
            if (category.getQuestions().length == 0) removeList.add(category.getName());
        }
        
        // remove the empty categories
        for(String name : removeList) {
            categoryMap.remove(name);
        }
    }
}
