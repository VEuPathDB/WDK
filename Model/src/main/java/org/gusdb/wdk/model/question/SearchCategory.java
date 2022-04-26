package org.gusdb.wdk.model.question;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;

/**
 * The SearchCategory is used to group questions into tree structure, and it is
 * used in the search menu, web service list, frontpage bubbles etc.
 *
 * Currently, due to the implementation limitation on the add step popup, the
 * search category can have at most 3 levels deep.
 *
 * @author jerric
 */
public class SearchCategory extends WdkModelBase {

  public static final String USED_BY_WEBSITE = "website";
  public static final String USED_BY_WEBSERVICE = "webservice";
  public static final String USED_BY_DATASET = "dataset";

  private static final Logger LOG = Logger.getLogger(SearchCategory.class);

  private String _name;
  private String _displayName;
  private String _shortDisplayName;
  private String _description;
  private boolean _flattenInMenu;
  private String _parentRef;
  private SearchCategory _parent;
  private String _usedBy;
  private Map<String, SearchCategory> _children;
  private List<CategoryQuestionRef> _questionRefList;
  private Map<String, CategoryQuestionRef> _questionRefMap;
  private List<WdkModelText> _descriptions;

  public SearchCategory() {
    _questionRefList = new ArrayList<CategoryQuestionRef>();
    _questionRefMap = new LinkedHashMap<>();
    _children = new LinkedHashMap<String, SearchCategory>();
    _descriptions = new ArrayList<>();
    _flattenInMenu = false;
  }

  public String getName() {
    return _name;
  }

  public void setName(String name) {
    _name = name;
  }

  public String getDisplayName() {
    return _displayName;
  }

  public void setDisplayName(String displayName) {
    _displayName = displayName;
  }

  public String getShortDisplayName() {
    return (_shortDisplayName != null) ? _shortDisplayName : getDisplayName();
  }

  public void setShortDisplayName(String shortDisplayName) {
    _shortDisplayName = shortDisplayName;
  }

  public void setFlattenInMenu(boolean flattenInMenu) {
    _flattenInMenu = flattenInMenu;
  }

  public boolean isFlattenInMenu() {
    return _flattenInMenu;
  }

  public String getDescription() {
    return _description;
  }

  public void setDescription(String description) {
    _description = description;
  }

  public void addDescription(WdkModelText description) {
    _descriptions.add(description);
  }

  public void addQuestionRef(CategoryQuestionRef questionRef) {
    _questionRefList.add(questionRef);
  }

  // temporary method
  public void addResolvedQuestionRef(CategoryQuestionRef questionRef) {
    _questionRefMap.put(questionRef.getQuestionFullName(), questionRef);
  }
  
  /**
   * use this to force the set of questions, eg, if using externally determined sorting.
   * must be called after resolve references.
   * @param questionRefMap
   */
  public void setResolvedQuestionRefMap(Map<String, CategoryQuestionRef> questionRefMap) {
    _questionRefMap = questionRefMap;
  }
  
  public Collection<CategoryQuestionRef> getQuestionRefs() {
    return _questionRefMap.values();
  }

  public Question[] getWebsiteQuestions() {
    return getQuestions(USED_BY_WEBSITE, false);
  }

  public Question[] getWebserviceQuestions() {
    return getQuestions(USED_BY_WEBSERVICE, false);
  }

  public Question[] getDatasetQuestions() {
    return getQuestions(USED_BY_DATASET, true);
  }

  private Question[] getQuestions(String usedBy, boolean strict) {
    List<Question> questions = new ArrayList<Question>();
    for (CategoryQuestionRef questionRef : _questionRefMap.values()) {
      if (questionRef.isUsedBy(usedBy, strict)) {
        String questionName = questionRef.getQuestionFullName();
        try {
          Question question = (Question) _wdkModel.resolveReference(questionName);
          questions.add(question);
        } 
        catch (WdkModelException e ){
          LOG.debug("************* question not resolved: " + questionName);
        }
      }
    }
    Question[] array = new Question[questions.size()];
    questions.toArray(array);
    return array;
  }

  public void setParentRef(String parentRef) {
    _parentRef = parentRef;
  }

  public SearchCategory getParent() {
    return _parent;
  }

  public Map<String, SearchCategory> getWebsiteChildren() {
    return getChildren(USED_BY_WEBSITE, false);
  }

  public Map<String, SearchCategory> getWebserviceChildren() {
    return getChildren(USED_BY_WEBSERVICE, false);
  }

  public Map<String, SearchCategory> getDatasetChildren() {
    return getChildren(USED_BY_DATASET, true);
  }

  private Map<String, SearchCategory> getChildren(String usedBy, boolean strict) {
    Map<String, SearchCategory> categories = new LinkedHashMap<String, SearchCategory>();
    for (SearchCategory child : _children.values()) {
      String cusedBy = child.getUsedBy();
      if ((strict && usedBy != null && cusedBy != null && cusedBy.equalsIgnoreCase(usedBy)) ||
          (usedBy == null || cusedBy == null || cusedBy.equalsIgnoreCase(usedBy))) {
        categories.put(child.getName(), child);
      }
    }
    return categories;
  }

  public boolean isAncesterOf(SearchCategory category) {
    SearchCategory parent = category._parent;
    while (parent != null) {
      if (parent.equals(this))
        return true;
      parent = parent._parent;
    }
    return false;
  }

  @Override
  public void excludeResources(String projectId) throws WdkModelException {
    for (CategoryQuestionRef ref : _questionRefList) {
      if (ref.include(projectId)) {
        String questionName = ref.getQuestionFullName();
        if (_questionRefMap.containsKey(questionName))
          LOG.warn("Duplicate question reference '"
              + questionName + "' detected in searchCategory '" + getName()
              + "'");
        ref.excludeResources(projectId);
        _questionRefMap.put(questionName, ref);
      }
    }
    _questionRefList.clear();
    _questionRefList = null;

    // exclude descriptions
    for (WdkModelText text : _descriptions) {
      if (text.include(projectId)) {
        if (_description != null)
          throw new WdkModelException("Duplicated descriptions detected in "
              + "searchCategory '" + getName() + "'");
        text.excludeResources(projectId);
        _description = text.getText();
      }
    }
    _descriptions.clear();
    _descriptions = null;
  }

  @Override
  public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
    _wdkModel = wdkModel;
    // get try resolving the question
    List<String> toRemove = new ArrayList<>();
    for (String key : _questionRefMap.keySet()) {
      CategoryQuestionRef ref = _questionRefMap.get(key);
      String questionName = ref.getQuestionFullName();
      try {
        wdkModel.resolveReference(questionName);
      } catch (WdkModelException ex) {
        // relax a bit, just ignore the missing questions
        LOG.debug("The question [" + questionName + "] is defined "
            + "in category [" + _name + "], but doesn't exist in "
            + "the model.");
        toRemove.add(key);
      }
    }
    for (String key : toRemove) {
      _questionRefMap.remove(key);
    }

    // resolve the parent
    SearchCategory parent = wdkModel.getCategories().get(_parentRef);
    if (parent != null) {
      // parent cannot be the same node as this one, or a child of it
      if (parent.equals(this) || this.isAncesterOf(parent))
        throw new WdkModelException("the category '" + _name
            + "' cannot have a parent of '" + _parentRef + "'");
      _parent = parent;
      _parent._children.put(_name, this);
    }
  }
  
  /**
   * an alternate means to build the tree
   * @param kid
   */
  public void addChild(SearchCategory kid) {
      _children.put(kid.getName(), kid);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof SearchCategory))
      return false;
    SearchCategory category = (SearchCategory) obj;
    return _name.equalsIgnoreCase(category._name);
  }

  @Override
  public int hashCode() {
    return (_name != null) ? _name.toLowerCase().hashCode() : 0;
  }

  public boolean isMultiCategory() {
    return _children.size() > 1;
  }

  public String getUsedBy() {
    return _usedBy;
  }

  public void setUsedBy(String usedBy) {
    _usedBy = usedBy;
  }

  public boolean isUsedBy(String usedBy) {
    return isUsedBy(usedBy, false);
  }

  public boolean isUsedBy(String usedBy, boolean strict) {
    if (strict)
      return (usedBy != null && _usedBy != null && _usedBy.equalsIgnoreCase(usedBy));
    return (usedBy == null || _usedBy == null || _usedBy.equalsIgnoreCase(usedBy));
  }

  public boolean hasQuestion(String questionFullName, String usedBy) {
    CategoryQuestionRef ref = _questionRefMap.get(questionFullName);
    if (ref == null)
      return false;
    return ref.isUsedBy(usedBy);
  }

  public void prettyPrint(StringBuilder builder, String indent) {
    builder.append(indent + getName() + " : " + getDisplayName() + System.lineSeparator());
    for (String name : _questionRefMap.keySet()) builder.append(indent + "  --" + name + System.lineSeparator());
    for (SearchCategory kid : _children.values()) kid.prettyPrint(builder, indent + "|");
  }

  // temporary method for EuPathCategoriesFactory
  public void setWdkModel(WdkModel model) {
    _wdkModel = model;
  }
}
