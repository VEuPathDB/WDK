/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.question.SearchCategory;

/**
 * @author xingao
 * 
 */
public class CategoryBean {

    private SearchCategory category;

    public CategoryBean(SearchCategory category) {
        this.category = category;
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.question.SearchCategory#getChildren()
     */
    public Map<String, CategoryBean> getWebsiteChildren() {
        Map<String, CategoryBean> beans = new LinkedHashMap<String, CategoryBean>();
        for (SearchCategory child : category.getWebsiteChildren().values()) {
            beans.put(child.getName(), new CategoryBean(child));
        }
        return beans;
    }

    public Map<String, CategoryBean> getWebserviceChildren() {
        Map<String, CategoryBean> beans = new LinkedHashMap<String, CategoryBean>();
        for (SearchCategory child : category.getWebserviceChildren().values()) {
            beans.put(child.getName(), new CategoryBean(child));
        }
        return beans;
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.question.SearchCategory#getDisplayName()
     */
    public String getDisplayName() {
        return category.getDisplayName();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.question.SearchCategory#getName()
     */
    public String getName() {
        return category.getName();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.question.SearchCategory#isFlattenInMenu()
     */
    public boolean isFlattenInMenu() {
        return category.isFlattenInMenu();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.question.SearchCategory#getParent()
     */
    public CategoryBean getParent() {
        return new CategoryBean(category.getParent());
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.question.SearchCategory#getQuestions()
     */
    public QuestionBean[] getWebsiteQuestions() throws WdkModelException {
        Question[] questions = category.getWebsiteQuestions();
        QuestionBean[] beans = new QuestionBean[questions.length];
        for (int i = 0; i < questions.length; i++) {
            beans[i] = new QuestionBean(questions[i]);
        }
        return beans;
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.question.SearchCategory#getQuestions()
     */
    public QuestionBean[] getWebserviceQuestions() throws WdkModelException {
        Question[] questions = category.getWebserviceQuestions();
        QuestionBean[] beans = new QuestionBean[questions.length];
        for (int i = 0; i < questions.length; i++) {
            beans[i] = new QuestionBean(questions[i]);
        }
        return beans;
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.question.SearchCategory#isMutliCategory()
     */
    public boolean isMultiCategory() {
        return category.isMultiCategory();
    }

}
