/**
 * 
 */
package org.gusdb.wdk.model.xml;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.ModelSetI;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;

/**
 * @author Jerric
 * @created Oct 14, 2005
 */
public class XmlQuestionSet extends WdkModelBase implements ModelSetI {

    private String name;
    private String displayName;

    private List<WdkModelText> descriptions = new ArrayList<WdkModelText>();
    private String description;

    private boolean isInternal;

    private List<XmlQuestion> questionList = new ArrayList<XmlQuestion>();
    private Map<String, XmlQuestion> questions =
            new LinkedHashMap<String, XmlQuestion>();

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.ModelSetI#getName()
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * @param description
     *                The description to set.
     */
    public void addDescription(WdkModelText description) {
        this.descriptions.add(description);
    }

    /**
     * @return Returns the displayName.
     */
    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * @param displayName
     *                The displayName to set.
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @return Returns the isInternal.
     */
    public boolean isInternal() {
        return this.isInternal;
    }

    /**
     * @param isInternal
     *                The isInternal to set.
     */
    public void setInternal(boolean isInternal) {
        this.isInternal = isInternal;
    }

    public XmlQuestion getQuestion(String name) throws WdkModelException {
        XmlQuestion question = questions.get(name);
        if (question == null)
            throw new WdkModelException("Question " + name
                    + " not found in set " + getName());
        return question;
    }

    public XmlQuestion[] getQuestions() {
        XmlQuestion[] quesArray = new XmlQuestion[questions.size()];
        questions.values().toArray(quesArray);
        return quesArray;
    }

    public void addQuestion(XmlQuestion question) {
        questionList.add(question);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.ModelSetI#getElement(java.lang.String)
     */
    public Object getElement(String elementName) {
        return questions.get(elementName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.ModelSetI#resolveReferences(org.gusdb.wdk.model.WdkModel)
     */
    public void resolveReferences(WdkModel model) throws WdkModelException {
        // resolve the references of questions
        for (XmlQuestion question : questions.values()) {
            question.resolveReferences(model);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.ModelSetI#setResources(org.gusdb.wdk.model.WdkModel)
     */
    public void setResources(WdkModel model) throws WdkModelException {
        // set resources for questions
        for (XmlQuestion question : questions.values()) {
            question.setQuestionSet(this);
            question.setResources(model);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer("QuestionSet: name='");
        buf.append(getName());
        buf.append("'\r\n\tdisplayName='");
        buf.append(getDisplayName());
        buf.append("'\r\n\tdescription='");
        buf.append(getDescription());
        buf.append("'\r\n\r\n");

        for (XmlQuestion question : questions.values()) {
            buf.append("\r\n:::::::::::::::::::::::::::::::::::::::::::::\r\n");
            buf.append(question);
            buf.append("\r\n");
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
                    throw new WdkModelException("The xmlQuestionSet "
                            + getName() + " has more than one description "
                            + "for project " + projectId);
                } else {
                    this.description = description.getText();
                    hasDescription = true;
                }
            }
        }
        descriptions = null;

        // exclude xml questions
        for (XmlQuestion question : questionList) {
            if (question.include(projectId)) {
                question.setQuestionSet(this);
                question.excludeResources(projectId);
                String questionName = question.getName();
                if (questions.containsKey(questionName))
                    throw new WdkModelException("The xmlQuestion "
                            + questionName + " is duplicated in the "
                            + "xmlQuestionSet " + this.name);
                questions.put(questionName, question);
            }
        }
        questionList = null;
    }
}
