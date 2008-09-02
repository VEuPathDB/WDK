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
    private boolean visible;

    private List<WdkModelText> descriptionList = new ArrayList<WdkModelText>();
    private String description;

    private List<AnswerFilterLayoutInstance> instanceList = new ArrayList<AnswerFilterLayoutInstance>();
    private Map<String, AnswerFilterInstance> instanceMap = new LinkedHashMap<String, AnswerFilterInstance>();
    private Map<String, String> layoutMap = new LinkedHashMap<String, String>();

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

    public void addInstance(AnswerFilterLayoutInstance instance) {
        instance.setRecordClass(recordClass);
        this.instanceList.add(instance);
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
        List<AnswerFilterLayoutInstance> newInstances = new ArrayList<AnswerFilterLayoutInstance>();
        for (AnswerFilterLayoutInstance instance : instanceList) {
            if (instance.include(projectId)) {
                instance.excludeResources(projectId);
                newInstances.add(instance);
            }
        }
        instanceList = newInstances;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.WdkModelBase#resolveReferences(org.gusdb.wdk.model.WdkModel)
     */
    @Override
    public void resolveReferences(WdkModel wodkModel) throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        if (resolved) return;
        // resolve the instances
        for (AnswerFilterLayoutInstance instance : instanceList) {
            instance.resolveReferences(wodkModel);
            String ref = instance.getRef();
            if (layoutMap.containsKey(ref))
                throw new WdkModelException("The same filter instance [" + ref
                        + "] is referenced more than once " + "in the layout ["
                        + name + "]");
            layoutMap.put(instance.getRef(), instance.getLayout());
            instanceMap.put(ref, instance.getInstance());
        }
        instanceList = null;

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

    public Map<String, String> getlayoutMap() {
        return new LinkedHashMap<String, String>(layoutMap);
    }
}
