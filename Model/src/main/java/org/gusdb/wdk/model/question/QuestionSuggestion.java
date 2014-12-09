/**
 * 
 */
package org.gusdb.wdk.model.question;

import org.gusdb.wdk.model.WdkModelBase;

/**
 * @author Jerric
 *
 */
public class QuestionSuggestion extends WdkModelBase {

  private String _newBuild;
  private String _reviseBuild;
  /**
   * 
   */
  public QuestionSuggestion() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param base
   */
  public QuestionSuggestion(QuestionSuggestion suggestion) {
    super(suggestion);
    this._newBuild = suggestion._newBuild;
    this._reviseBuild = suggestion._reviseBuild;
  }

  public String getNewBuild() {
    return _newBuild;
  }

  public void setNewBuild(String newBuild) {
    _newBuild = newBuild;
  }

  public String getReviseBuild() {
    return _reviseBuild;
  }

  public void setReviseBuild(String reviseBuild) {
    _reviseBuild = reviseBuild;
  }

  
}
