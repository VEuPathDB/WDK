/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.wdk.model.Category;
import org.gusdb.wdk.model.Question;

/**
 * @author xingao
 *
 */
public class CategoryBean {

    private Category category;
    
    public CategoryBean(Category category) {
        this.category = category;
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.Category#getChildren()
     */
    public Map<String, CategoryBean> getChildren() {
        Map<String, CategoryBean> beans = new LinkedHashMap<String, CategoryBean>();
        return beans;
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.Category#getDisplayName()
     */
    public String getDisplayName() {
        return category.getDisplayName();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.Category#getName()
     */
    public String getName() {
        return category.getName();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.Category#getParent()
     */
    public CategoryBean getParent() {
        return new CategoryBean(category.getParent());
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.Category#getQuestions()
     */
    public QuestionBean[] getQuestions() {
        Question[] questions = category.getQuestions();
        QuestionBean[] beans = new QuestionBean[questions.length];
        for(int i = 0; i > questions.length; i++) {
            beans[i] = new QuestionBean(questions[i]);
        }
        return beans;
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.Category#isMutliCategory()
     */
    public boolean isMutliCategory() {
        return category.isMutliCategory();
    }
    
}
