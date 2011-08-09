package org.gusdb.wdk.model.jspwrap;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.QuestionSet;

/**
 * A wrapper on a {@link QuestionSet} that provides simplified access for
 * consumption by a view
 */
public class QuestionSetBean {

    QuestionSet questionSet;

    public QuestionSetBean(QuestionSet questionSet) {
        this.questionSet = questionSet;
    }

    public QuestionBean[] getQuestions() {
        Question[] questions = questionSet.getQuestions();
        QuestionBean[] questionBeans = new QuestionBean[questions.length];
        for (int i = 0; i < questions.length; i++) {
            questionBeans[i] = new QuestionBean(questions[i]);
        }
        return questionBeans;
    }

    public Map<String, QuestionBean> getQuestionsMap() {
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

    @Deprecated
    public Map<String, Set<QuestionBean>> getQuestionsByCategory() {
        Map<String, Set<QuestionBean>> questions = new LinkedHashMap<String, Set<QuestionBean>>();
        Question[] qs = questionSet.getQuestions();
        for (Question q : qs) {
            String category = q.getCategory();
            if (category == null || category.length() == 0) category = " ";
            Set<QuestionBean> subqs = questions.get(category);
            if (subqs == null) {
                subqs = new LinkedHashSet<QuestionBean>();
                questions.put(category, subqs);
            }
            subqs.add(new QuestionBean(q));
        }
        return questions;
    }
}
