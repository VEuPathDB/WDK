/**
 * 
 */
package org.gusdb.wdk.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xingao
 * 
 */
public class AnswerFilterLayout extends WdkModelBase {

    private RecordClass recordClass;

    private String name;
    private String displayName;
    private boolean visible = true;
    private String fileName;
    private boolean vertical = false;

    private List<WdkModelText> descriptionList = new ArrayList<WdkModelText>();
    private String description;

    private List<AnswerFilterInstanceReference> referenceList = new ArrayList<AnswerFilterInstanceReference>();
    private Map<String, AnswerFilterInstance> instanceMap = new LinkedHashMap<String, AnswerFilterInstance>();

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
     */
    /**
     * @return the recordClass
     */
    public RecordClass getRecordClass() {
        return recordClass;
    }

    /**
     * @param recordClass
     *            the recordClass to set
     */
    void setRecordClass(RecordClass recordClass) {
        this.recordClass = recordClass;
        if (referenceList != null) {
            for (AnswerFilterInstanceReference reference : referenceList) {
                reference.setRecordClass(recordClass);
            }
        }
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the displayName
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @param displayName
     *            the displayName to set
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @return the visible
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * @param visible
     *            the visible to set
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void addDescription(WdkModelText description) {
        this.descriptionList.add(description);
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    public void addReference(AnswerFilterInstanceReference reference) {
        if (recordClass != null) reference.setRecordClass(recordClass);
        this.referenceList.add(reference);
    }

    @Override
    public void excludeResources(String projectId) throws WdkModelException {
        // exclude the descriptions
        for (WdkModelText text : descriptionList) {
            if (text.include(projectId)) {
                text.excludeResources(projectId);
                if (description != null)
                    throw new WdkModelException("Description of "
                            + "answerFilterLayout '" + name + "' in "
                            + recordClass.getFullName()
                            + " is included more than once.");
                this.description = text.getText();
            }
        }
        descriptionList = null;

        // exclude the instances
        List<AnswerFilterInstanceReference> newReferences = new ArrayList<AnswerFilterInstanceReference>();
        for (AnswerFilterInstanceReference reference : referenceList) {
            if (reference.include(projectId)) {
                reference.excludeResources(projectId);
                newReferences.add(reference);
            }
        }
        referenceList = newReferences;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.WdkModelBase#resolveReferences(org.gusdb.wdk.model
     * .WdkModel)
     */
    @Override
    public void resolveReferences(WdkModel wodkModel) throws WdkModelException {
        if (resolved) return;

        // resolve the instances
        for (AnswerFilterInstanceReference reference : referenceList) {
            reference.resolveReferences(wodkModel);
            String ref = reference.getRef();
            if (instanceMap.containsKey(ref))
                throw new WdkModelException("More than one instance [" + ref
                        + "] are defined in filter layout [" + name + "]");
            instanceMap.put(ref, reference.getInstance());
        }
        referenceList = null;

        resolved = true;
    }

    public Map<String, AnswerFilterInstance> getInstanceMap() {
        return new LinkedHashMap<String, AnswerFilterInstance>(instanceMap);
    }

    public AnswerFilterInstance[] getInstances() {
        AnswerFilterInstance[] array = new AnswerFilterInstance[instanceMap.size()];
        instanceMap.values().toArray(array);
        return array;
    }

    /**
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @param fileName
     *            the fileName to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
        if (this.fileName != null) this.fileName = this.fileName.trim();
    }

    /**
     * @return the vertical
     */
    public boolean isVertical() {
        return vertical;
    }

    /**
     * @param vertical
     *            the vertical to set
     */
    public void setVertical(boolean vertical) {
        this.vertical = vertical;
    }

}
