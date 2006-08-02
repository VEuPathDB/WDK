/**
 * 
 */
package org.gusdb.wdk.controller.action;

import java.util.HashMap;
import java.util.Map;

import org.apache.struts.action.ActionForm;


/**
 * @author xingao
 *
 */
public class ShowProfileForm extends ActionForm {

    /**
     * 
     */
    private static final long serialVersionUID = 4492820263998851639L;
    private Map<String, String> globalPreferences;
    private Map<String, String> projectPreferences;
    
    public ShowProfileForm() {
        globalPreferences = new HashMap<String, String>();
        projectPreferences = new HashMap<String, String>();
    }
    
    public void addGlobalPreferences(Map<String, String> prefs) {
        globalPreferences.clear();
        globalPreferences.putAll(prefs);
    }
    
    public void addProjectPreferences(Map<String, String> prefs) {
        projectPreferences.clear();
        projectPreferences.putAll(prefs);
    }
    
    public String getGlobalPreference(String prefName) {
        // TEST
        System.out.println("Global Preferences: " + globalPreferences.size());
        
        return globalPreferences.get(prefName);
    }
    
    public String getProjectPreference(String prefName) {
        return projectPreferences.get(prefName);
    }
}
