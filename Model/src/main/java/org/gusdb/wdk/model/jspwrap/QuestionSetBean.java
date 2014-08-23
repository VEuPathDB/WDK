package org.gusdb.wdk.model.jspwrap;

import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.question.QuestionSet;

/**
 * A wrapper on a {@link QuestionSet} that provides simplified access for
 * consumption by a view
 */
public class QuestionSetBean {

    QuestionSet questionSet;

    public QuestionSetBean(QuestionSet questionSet) {
        this.questionSet = questionSet;
    }

    public QuestionBean[] getQuestions() throws WdkModelException {
        Question[] questions = questionSet.getQuestions();
        QuestionBean[] questionBeans = new QuestionBean[questions.length];
        for (int i = 0; i < questions.length; i++) {
            questionBeans[i] = new QuestionBean(questions[i]);
        }
        return questionBeans;
    }

    public Map<String, QuestionBean> getQuestionsMap() throws WdkModelException {
        LinkedHashMap<String, QuestionBean> map = new LinkedHashMap<String, QuestionBean>();
        QuestionBean[] questions = getQuestions();
        for (int i = 0; i < questions.length; i++) {
            map.put(questions[i].getName(), questions[i]);
        }
        return map;
    }

    public String getName() {
        return questionSet.getName();
    }

    public boolean isInternal() {
        return questionSet.isInternal();
    }

    public String getDisplayName() {
        return questionSet.getDisplayName();
    }

    public String getDescription() {
        return questionSet.getDescription();
    }
}
