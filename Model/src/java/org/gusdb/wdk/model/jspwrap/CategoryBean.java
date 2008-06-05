/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

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
     * @see org.gusdb.wdk.model.Category#getQuestions()
     */
    public QuestionBean[] getQuestions() {
        Question[] questions = category.getQuestions();
        QuestionBean[] questionBeans = new QuestionBean[questions.length];
        for(int i = 0; i < questions.length; i++) {
            questionBeans[i] = new QuestionBean(questions[i]);
        }
        return questionBeans;
    }
    
}
