/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.Categories;
import org.gusdb.wdk.model.Category;

/**
 * @author xingao
 *
 */
public class CategoriesBean {

    private Categories categories;
    
    public CategoriesBean(Categories categories) {
        this.categories = categories;
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.Categories#getCategories()
     */
    public CategoryBean[] getCategories() {
        Category[] cats = categories.getCategories();
        CategoryBean[] catBeans = new CategoryBean[cats.length];
        for(int i = 0; i < cats.length; i++) {
            catBeans[i] = new CategoryBean(cats[i]);
        }
        return catBeans;
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.Categories#getRecordClass()
     */
    public RecordClassBean getRecordClass() {
        return new RecordClassBean(categories.getRecordClass());
    }
}
