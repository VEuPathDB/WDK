package org.gusdb.wdk.model.record.attribute.plugin;

import java.util.Map;

import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.user.Step;

/**
 * @author jerric
 * 
 *         The plugin is created per request, therefore request-scoped objects
 *         can be set into it.
 */
public interface AttributePlugin {

    String getName();

    void setName(String name);

    String getDisplay();

    void setDisplay(String display);
    
    String getView();
    
    void setView(String view);
    
    String getDescription();
    
    void setDescription(String description);

    void setProperties(Map<String, String> properties);

    void setAttributeField(AttributeField attribute);

    void setStep(Step step);

    Map<String, Object> process();
    
    String getDownloadContent();
}
