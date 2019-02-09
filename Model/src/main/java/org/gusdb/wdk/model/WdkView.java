package org.gusdb.wdk.model;

import java.util.ArrayList;
import java.util.List;

public abstract class WdkView extends WdkModelBase {

    private String _name;
    private String _display;
    private boolean _default;

    private List<WdkModelText> _descriptions;
    private String _description;

    public WdkView() {
        _description = "";
        _descriptions = new ArrayList<WdkModelText>();
    }

    /**
     * @return the name
     */
    public String getName() {
        return _name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        _name = name;
    }

    /**
     * @return the display
     */
    public String getDisplay() {
        return (_display == null) ? _name : _display;
    }

    /**
     * @param display
     *            the display to set
     */
    public void setDisplay(String display) {
        _display = display;
    }

    /**
     * @return the _default
     */
    public boolean isDefault() {
        return _default;
    }

    /**
     * @param _default
     *            the _default to set
     */
    public void setDefault(boolean defaultValue) {
        _default = defaultValue;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return _description;
    }

    /**
     * @param description
     *            the description to set
     */
    public void addDescription(WdkModelText description) {
        _descriptions.add(description);
    }

    @Override
    public void excludeResources(String projectId) throws WdkModelException {
        // exclude descriptions
        boolean hasDescription = false;
        for (WdkModelText description : _descriptions) {
            if (description.include(projectId)) {
                if (hasDescription) {
                    throw new WdkModelException("The view " + getName()
                            + " has more than one description for project "
                            + projectId);
                } else {
                    _description = description.getText();
                    hasDescription = true;
                }
            }
        }
        _descriptions = null;
    }
}
