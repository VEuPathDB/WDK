/**
 * 
 */
package org.gusdb.wdk.model;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;

/**
 * @author xingao
 * 
 */
public class AnswerFilterLayout extends WdkModelBase {

    private RecordClass recordClass;

    private String name;
    private String displayName;
    private boolean visible = true;

    private List<WdkModelText> descriptionList = new ArrayList<WdkModelText>();
    private String description;

    private List<WdkModelText> layoutList = new ArrayList<WdkModelText>();
    private String layout;

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

    public void addLayout(WdkModelText layout) {
        this.layoutList.add(layout);
    }

    public String getLayout() {
        return layout;
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

        // exclude the labels
        for (WdkModelText layout : layoutList) {
            if (layout.include(projectId)) {
                layout.excludeResources(projectId);
                if (this.layout != null)
                    throw new WdkModelException("The layout is defined more "
                            + "than once in filter layout [" + name + "]");
                this.layout = layout.getText();
            }
        }
        layoutList = null;
        // make sure the layout is defined
        if (layout == null)
            throw new WdkModelException("The layout is not defined in "
                    + "filter layout [" + name + "]");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.WdkModelBase#resolveReferences(org.gusdb.wdk.model
     * .WdkModel)
     */
    @Override
    public void resolveReferences(WdkModel wodkModel) throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
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
}
