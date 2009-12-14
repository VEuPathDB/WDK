package org.gusdb.wdk.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class Category extends WdkModelBase {

    public static final String USED_BY_WEBSITE = "website";
    public static final String USED_BY_WEBSERVICE = "webservice";

    private static final Logger logger = Logger.getLogger(Category.class);

    private WdkModel wdkModel;
    private String name;
    private String displayName;
    private String parentRef;
    private Category parent;
    private String usedBy;
    private Map<String, Category> children;
    private List<CategoryQuestionRef> questionRefs;

    public Category() {
        questionRefs = new ArrayList<CategoryQuestionRef>();
        children = new LinkedHashMap<String, Category>();
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

    public void addQuestionRef(CategoryQuestionRef questionRef) {
        this.questionRefs.add(questionRef);
    }

    public Question[] getWebsiteQuestions() throws WdkModelException {
        return getQuestions(USED_BY_WEBSITE);
    }

    public Question[] getWebserviceQuestions() throws WdkModelException {
        return getQuestions(USED_BY_WEBSERVICE);
    }

    private Question[] getQuestions(String usedBy) throws WdkModelException {
        List<Question> questions = new ArrayList<Question>();
        for (CategoryQuestionRef questionRef : questionRefs) {
            String qusedBy = questionRef.getUsedBy();
            if (usedBy == null || qusedBy == null
                    || qusedBy.equalsIgnoreCase(usedBy)) {
                String ref = questionRef.getText();
                Question question = (Question) wdkModel.resolveReference(ref);
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

    public Category getParent() {
        return parent;
    }

    public Map<String, Category> getWebsiteChildren() {
        return getChildren(USED_BY_WEBSITE);
    }
    
    public Map<String, Category> getWebserviceChildren() {
        return getChildren(USED_BY_WEBSERVICE);
    }
    
    private Map<String, Category> getChildren(String usedBy) {
        Map<String, Category> categories = new LinkedHashMap<String, Category>();
        for(Category child : children.values()) {
            String cusedBy = child.getUsedBy();
            if (usedBy == null || cusedBy == null || cusedBy.equalsIgnoreCase(usedBy)){
                categories.put(child.getName(), child);
            }
        }
        return categories;
    }

    public boolean isAncesterOf(Category category) {
        Category parent = category.parent;
        while (parent != null) {
            if (parent.equals(this)) return true;
            parent = parent.parent;
        }
        return false;
    }

    @Override
    public void excludeResources(String projectId) throws WdkModelException {
        // exclude questionRefs
        for (int i = questionRefs.size() -1; i >=0; i--) {
            CategoryQuestionRef ref = questionRefs.get(i);
            if (ref.include(projectId)) {
                ref.excludeResources(projectId);
            } else {
                questionRefs.remove(i);
            }
        }
    }

    public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
        this.wdkModel = wdkModel;
        // get the base recordClass
        for (int i = questionRefs.size() -1; i >=0; i--) {
            CategoryQuestionRef ref = questionRefs.get(i);
            String questionName = ref.getText().trim();
            try {
                wdkModel.resolveReference(questionName);
            } catch (WdkModelException ex) {
                // relax a bit, just ignore the missing questions
                logger.debug("The question [" + questionName + "] is defined "
                        + "in category [" + name + "], but doesn't exist in "
                        + "the model.");
                questionRefs.remove(i);
            }
        }

        // resolve the parent
        Category parent = wdkModel.getCategories().get(parentRef);
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
        if (obj == null || !(obj instanceof Category)) return false;
        Category category = (Category) obj;
        return this.name.equalsIgnoreCase(category.name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return this.name.toLowerCase().hashCode();
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
}
