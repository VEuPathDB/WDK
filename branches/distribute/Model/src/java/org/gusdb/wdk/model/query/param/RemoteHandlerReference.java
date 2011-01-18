package org.gusdb.wdk.model.query.param;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;

public class RemoteHandlerReference extends WdkModelBase {

    private String handlerClass;
    private List<WdkModelText> propertyList = new ArrayList<WdkModelText>();
    private Map<String, String> propertyMap;

    /**
     * @return the handlerClass
     */
    public String getHandlerClass() {
        return handlerClass;
    }

    /**
     * @param handlerClass
     *            the handlerClass to set
     */
    public void setHandler(String handlerClass) {
        this.handlerClass = handlerClass;
    }
    
    public void addProperty(WdkModelText property) {
        this.propertyList.add(property);
    }
    
    public Map<String, String> getProperties() {
        return propertyMap;
    }

    @Override
    public void excludeResources(String projectId) throws WdkModelException {
        super.excludeResources(projectId);

        propertyMap = new LinkedHashMap<String, String>();
        for (WdkModelText property : propertyList) {
            if (property.include(projectId)) {
                property.excludeResources(projectId);
                String name = property.getName();
                if (propertyMap.containsKey(name))
                    throw new WdkModelException("The setting entry '" + name
                            + "' is duplicate in the remoteHandler "
                            + handlerClass);
                propertyMap.put(name, property.getText());
            }
        }
        propertyList = null;
    }
}
