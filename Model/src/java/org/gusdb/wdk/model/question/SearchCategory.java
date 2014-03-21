package org.gusdb.wdk.model.question;

import java.util.ArrayList;
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
 * 
 */
public class SearchCategory extends WdkModelBase {

  public static final String USED_BY_WEBSITE = "website";
  public static final String USED_BY_WEBSERVICE = "webservice";
  public static final String USED_BY_DATASET = "dataset";

  private static final Logger logger = Logger.getLogger(SearchCategory.class);

  private WdkModel wdkModel;
  private String name;
  private String displayName;
  private String shortDisplayName;
  private String description;
  private boolean flattenInMenu;
  private String parentRef;
  private SearchCategory parent;
  private String usedBy;
  private Map<String, SearchCategory> children;
  private List<CategoryQuestionRef> questionRefList;
  private Map<String, CategoryQuestionRef> questionRefMap;
  private List<WdkModelText> descriptions;

  public SearchCategory() {
    questionRefList = new ArrayList<CategoryQuestionRef>();
    questionRefMap = new LinkedHashMap<>();
    children = new LinkedHashMap<String, SearchCategory>();
    descriptions = new ArrayList<>();
    flattenInMenu = false;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getShortDisplayName() {
    return (shortDisplayName != null) ? shortDisplayName : getDisplayName();
  }

  public void setShortDisplayName(String shortDisplayName) {
    this.shortDisplayName = shortDisplayName;
  }

  public void setFlattenInMenu(boolean flattenInMenu) {
    this.flattenInMenu = flattenInMenu;
  }

  public boolean isFlattenInMenu() {
    return this.flattenInMenu;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void addDescription(WdkModelText description) {
    descriptions.add(description);
  }

  public void addQuestionRef(CategoryQuestionRef questionRef) {
    this.questionRefList.add(questionRef);
  }

  public Question[] getWebsiteQuestions() throws WdkModelException {
    return getQuestions(USED_BY_WEBSITE, false);
  }

  public Question[] getWebserviceQuestions() throws WdkModelException {
    return getQuestions(USED_BY_WEBSERVICE, false);
  }

  public Question[] getDatasetQuestions() throws WdkModelException {
    return getQuestions(USED_BY_DATASET, true);
  }

  private Question[] getQuestions(String usedBy, boolean strict) throws WdkModelException {
    List<Question> questions = new ArrayList<Question>();
    for (CategoryQuestionRef questionRef : questionRefMap.values()) {
      if (questionRef.isUsedBy(usedBy, strict)) {
        String questionName = questionRef.getQuestionFullName();
        Question question = (Question) wdkModel.resolveReference(questionName);
        questions.add(question);
      }
    }
    Question[] array = new Question[questions.size()];
    questions.toArray(array);
    return array;
  }

  public void setParentRef(String parentRef) {
    this.parentRef = parentRef;
  }

  public SearchCategory getParent() {
    return parent;
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
    for (SearchCategory child : children.values()) {
      String cusedBy = child.getUsedBy();
      if ((strict && usedBy != null && cusedBy != null && cusedBy.equalsIgnoreCase(usedBy)) ||
          (usedBy == null || cusedBy == null || cusedBy.equalsIgnoreCase(usedBy))) {
        categories.put(child.getName(), child);
      }
    }
    return categories;
  }

  public boolean isAncesterOf(SearchCategory category) {
    SearchCategory parent = category.parent;
    while (parent != null) {
      if (parent.equals(this))
        return true;
      parent = parent.parent;
    }
    return false;
  }

  @Override
  public void excludeResources(String projectId) throws WdkModelException {
    for (CategoryQuestionRef ref : questionRefList) {
      if (ref.include(projectId)) {
        String questionName = ref.getQuestionFullName();
        if (questionRefMap.containsKey(questionName))
          logger.warn("Duplicate question reference '"
              + questionName + "' detected in searchCategory '" + getName()
              + "'");
        ref.excludeResources(projectId);
        questionRefMap.put(questionName, ref);
      }
    }
    questionRefList.clear();
    questionRefList = null;

    // exclude descriptions
    for (WdkModelText text : descriptions) {
      if (text.include(projectId)) {
        if (description != null)
          throw new WdkModelException("Duplicated descriptions detected in "
              + "searchCategory '" + getName() + "'");
        text.excludeResources(projectId);
        description = text.getText();
      }
    }
    descriptions.clear();
    descriptions = null;
  }

  @Override
  public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
    this.wdkModel = wdkModel;
    // get try resolving the question
    List<String> toRemove = new ArrayList<>();
    for (String key : questionRefMap.keySet()) {
      CategoryQuestionRef ref = questionRefMap.get(key);
      String questionName = ref.getQuestionFullName();
      try {
        wdkModel.resolveReference(questionName);
      } catch (WdkModelException ex) {
        // relax a bit, just ignore the missing questions
        logger.debug("The question [" + questionName + "] is defined "
            + "in category [" + name + "], but doesn't exist in "
            + "the model.");
        toRemove.add(key);
      }
    }
    for (String key : toRemove) {
      questionRefMap.remove(key);
    }

    // resolve the parent
    SearchCategory parent = wdkModel.getCategories().get(parentRef);
    if (parent != null) {
      // parent cannot be the same node as this one, or a child of it
      if (parent.equals(this) || this.isAncesterOf(parent))
        throw new WdkModelException("the category '" + name
            + "' cannot have a parent of '" + parentRef + "'");
      this.parent = parent;
      this.parent.children.put(name, this);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof SearchCategory))
      return false;
    SearchCategory category = (SearchCategory) obj;
    return this.name.equalsIgnoreCase(category.name);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return (name != null) ? name.toLowerCase().hashCode() : 0;
  }

  public boolean isMultiCategory() {
    return children.size() > 1;
  }

  public String getUsedBy() {
    return usedBy;
  }

  public void setUsedBy(String usedBy) {
    this.usedBy = usedBy;
  }

  public boolean isUsedBy(String usedBy) {
    return isUsedBy(usedBy, false);
  }

  public boolean isUsedBy(String usedBy, boolean strict) {
    if (strict)
      return (usedBy != null && this.usedBy != null && this.usedBy.equalsIgnoreCase(usedBy));
    return (usedBy == null || this.usedBy == null || this.usedBy.equalsIgnoreCase(usedBy));
  }

  public boolean hasQuestion(String questionFullName, String usedBy) {
    CategoryQuestionRef ref = questionRefMap.get(questionFullName);
    if (ref == null)
      return false;
    return ref.isUsedBy(usedBy);
  }
}
