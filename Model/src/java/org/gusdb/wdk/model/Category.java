package org.gusdb.wdk.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class Category extends WdkModelBase {

    private static final Logger logger = Logger.getLogger(Category.class);

    private String name;
    private String displayName;
    private List<WdkModelText> questionRefs;
    private List<Question> questions;

    private String parentRef;
    private Category parent;
    private Map<String, Category> children = new LinkedHashMap<String, Category>();

    public Category() {
        questionRefs = new ArrayList<WdkModelText>();
        questions = new ArrayList<Question>();
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

    public void addQuestionRef(WdkModelText questionRef) {
        this.questionRefs.add(questionRef);
    }

    public Question[] getQuestions() {
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

    public Map<String, Category> getChildren() {
        return new LinkedHashMap<String, Category>(children);
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
        List<WdkModelText> temp = new ArrayList<WdkModelText>();
        for (WdkModelText ref : questionRefs) {
            if (ref.include(projectId)) {
                ref.excludeResources(projectId);
                temp.add(ref);
            }
        }
        questionRefs.clear();
        questionRefs = temp;
    }

    public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
        // get the base recordClass
        for (WdkModelText ref : questionRefs) {
            Question question = null;
            String questionName = ref.getText().trim();
            try {
                question = (Question) wdkModel.resolveReference(questionName);
            } catch (WdkModelException ex) {
                // relax a bit, just ignore the missing questions
                logger.debug("The question [" + questionName + "] is defined "
                        + "in category [" + name + "], but doesn't exist in "
                        + "the model.");
                continue;
            }
            questions.add(question);
        }
        questionRefs = null;

        // resolve the parent
        Category parent = wdkModel.getCategoryMap().get(parentRef);
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
        return questions.size() > 1;
    }
}
