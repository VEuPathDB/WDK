package org.gusdb.wdk.model.question;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.gusdb.wdk.model.ModelSetI;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;
import org.gusdb.wdk.model.test.sanity.OptionallyTestable;

/**
 * Question sets are used to organize questions into different groups.
 * 
 * Created: Fri June 4 15:05:30 2004 EDT
 * 
 * @author David Barkan
 * @version $Revision$ $Date$ $Author$
 */

public class QuestionSet extends WdkModelBase implements ModelSetI<Question>, OptionallyTestable {

  private List<Question> _questionList = new ArrayList<Question>();
  private Map<String, Question> _questionMap = new LinkedHashMap<String, Question>();
  private String _name;
  private String _displayName;

  private List<WdkModelText> _descriptions = new ArrayList<WdkModelText>();
  private String _description;
  private boolean _doNotTest = false;

  private boolean _internal = false;

  public void setName(String name) {
    _name = name.trim();
  }

  @Override
  public String getName() {
    return _name;
  }

  public void setDisplayName(String displayName) {
    _displayName = displayName;
  }

  public String getDisplayName() {
    return (_displayName != null) ? _displayName : _name;
  }

  public void addDescription(WdkModelText description) {
    _descriptions.add(description);
  }

  public String getDescription() {
    return _description;
  }

  public void setDoNotTest(boolean doNotTest) {
    _doNotTest = doNotTest;
  }

  @Override
  public boolean getDoNotTest() {
    return _doNotTest;
  }

  public boolean isInternal() {
    return _internal;
  }

  public void setInternal(boolean internal) {
    _internal = internal;
  }

  public Optional<Question> getQuestion(String name) {
    return Optional.ofNullable(_questionMap.get(name));
  }

  public boolean contains(String questionName) {
    return _questionMap.containsKey(questionName);
  }

  @Override
  public Question getElement(String name) {
    return _questionMap.get(name);
  }

  public Question[] getQuestions() {
    Question[] array = new Question[_questionMap.size()];
    _questionMap.values().toArray(array);
    return array;
  }

  public Map<String, Question> getQuestionMap() {
    return new LinkedHashMap<>(_questionMap);
  }

  public void addQuestion(Question question) {
    question.setQuestionSet(this);
    if (_questionList != null)
      _questionList.add(question);
    else
      _questionMap.put(question.getName(), question);
  }

  @Override
  public void resolveReferences(WdkModel model) throws WdkModelException {
    for (Question question : _questionMap.values()) {
      question.resolveReferences(model);
    }
  }

  @Override
  public void setResources(WdkModel model) throws WdkModelException {
    // do nothing
  }

  @Override
  public String toString() {
    String newline = System.getProperty("line.separator");
    StringBuffer buf = new StringBuffer("QuestionSet: name='" + getName() + "'" + newline +
        "  displayName='" + getDisplayName() + "'" + newline + "  description='" + getDescription() + "'" +
        newline + "  internal='" + isInternal() + "'" + newline);
    buf.append(newline);

    for (Question question : _questionMap.values()) {
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
    for (WdkModelText description : _descriptions) {
      if (description.include(projectId)) {
        if (hasDescription) {
          throw new WdkModelException(
              "The questionSet " + getName() + " has more than one description for project " + projectId);
        }
        else {
          _description = description.getText();
          hasDescription = true;
        }
      }
    }
    _descriptions = null;

    // exclude resources in each question
    for (Question question : _questionList) {
      if (question.include(projectId)) {
        question.setQuestionSet(this);
        question.excludeResources(projectId);
        String questionName = question.getName();
        if (_questionMap.containsKey(questionName))
          throw new WdkModelException(
              "Question named " + questionName + " already exists in question set " + getName());

        _questionMap.put(questionName, question);
      }
    }
    _questionList = null;
  }
}
