package org.gusdb.wdk.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * QuestionSet.java
 * 
 * Created: Fri June 4 15:05:30 2004 EDT
 * 
 * @author David Barkan
 * @version $Revision$ $Date: 2006-03-09 23:02:31 -0500 (Thu, 09 Mar
 *          2006) $ $Author$
 */

public class QuestionSet extends WdkModelBase implements ModelSetI {

    private List<Question> questionList = new ArrayList<Question>();
    private Map<String, Question> questions =
            new LinkedHashMap<String, Question>();
    private String name;
    private String displayName;

    private List<WdkModelText> descriptions = new ArrayList<WdkModelText>();
    private String description;

    private boolean internal = false;

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
        return (displayName != null) ? displayName : name;
    }

    public void addDescription(WdkModelText description) {
        this.descriptions.add(description);
    }

    public String getDescription() {
        return description;
    }

    public Boolean getInternal() {
        return this.internal;
    }

    public void setInternal(Boolean internal) {
        this.internal = internal;
    }

    public Question getQuestion(String name) throws WdkModelException {
        Question question = questions.get(name);
        if (question == null)
            throw new WdkModelException("Question Set " + getName()
                    + " does not include question " + name);
        return question;
    }

    public Object getElement(String name) {
        return questions.get(name);
    }

    public Question[] getQuestions() {
        Question[] array = new Question[questions.size()];
        questions.values().toArray(array);
        return array;
    }

    public Map<String, Question[]> getQuestionsByCategory() {
        Map<String, List<Question>> questionsByCategory =
                new LinkedHashMap<String, List<Question>>();
        for (Question question : questions.values()) {
            String category = question.getCategory();
            List<Question> questionList = questionsByCategory.get(category);
            if (questionList == null) {
                questionList = new ArrayList<Question>();
                questionsByCategory.put(category, questionList);
            }
            questionList.add(question);
        }

        Map<String, Question[]> questionArraysByCategory =
                new LinkedHashMap<String, Question[]>();
        for (String category : questionsByCategory.keySet()) {
            List<Question> questionList = questionsByCategory.get(category);
            Question[] questions = new Question[questionList.size()];
            questionList.toArray(questions);
            questionArraysByCategory.put(category, questions);
        }
        return questionArraysByCategory;
    }

    public void addQuestion(Question question) throws WdkModelException {
        questionList.add(question);
    }

    public void resolveReferences(WdkModel model) throws WdkModelException {
        for (Question question : questions.values()) {
            question.resolveReferences(model);
        }
    }

    public void setResources(WdkModel model) throws WdkModelException {
        for (Question question : questions.values()) {
            question.setResources(model);
            RecordClass rc = question.getRecordClass();
            rc.addQuestion(question);
        }

    }

    public String toString() {
        String newline = System.getProperty("line.separator");
        StringBuffer buf =
                new StringBuffer("QuestionSet: name='" + getName() + "'"
                        + newline + "  displayName='" + getDisplayName() + "'"
                        + newline + "  description='" + getDescription() + "'"
                        + newline + "  internal='" + getInternal() + "'"
                        + newline);
        buf.append(newline);

        for (Question question : questions.values()) {
            buf.append(newline);
            buf.append(":::::::::::::::::::::::::::::::::::::::::::::");
            buf.append(newline);
            buf.append(question);
            buf.append(newline);
        }

        return buf.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) throws WdkModelException {
        // exclude descriptions
        boolean hasDescription = false;
        for (WdkModelText description : descriptions) {
            if (description.include(projectId)) {
                if (hasDescription) {
                    throw new WdkModelException("The questionSet " + getName()
                            + " has more than one description for project "
                            + projectId);
                } else {
                    this.description = description.getText();
                    hasDescription = true;
                }
            }
        }
        descriptions = null;

        // exclude resources in each question
        for (Question question : questionList) {
            if (question.include(projectId)) {
                question.setQuestionSet(this);
                question.excludeResources(projectId);
                String questionName = question.getName();
                if (questions.containsKey(questionName))
                    throw new WdkModelException("Question named "
                            + questionName + " already exists in question set "
                            + getName());

                questions.put(questionName, question);
            }
        }
        questionList = null;
    }
}
