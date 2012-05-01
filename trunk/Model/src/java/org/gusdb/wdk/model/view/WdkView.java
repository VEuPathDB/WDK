package org.gusdb.wdk.model.view;

import java.util.List;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;

public abstract class WdkView extends WdkModelBase {

    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(WdkView.class.getName());

    private String name;
    private String display;
    private String jsp;
    private boolean _default;

    private List<WdkModelText> descriptions;
    private String description;

    public WdkView() {
        description = "";
        descriptions = new ArrayList<WdkModelText>();
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
     * @return the display
     */
    public String getDisplay() {
        return (display == null) ? name : display;
    }

    /**
     * @param display
     *            the display to set
     */
    public void setDisplay(String display) {
        this.display = display;
    }

    /**
     * @return the jsp
     */
    public String getJsp() {
        return jsp;
    }

    /**
     * @param jsp
     *            the jsp to set
     */
    public void setJsp(String jsp) {
        this.jsp = jsp;
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
    public void setDefault(boolean _default) {
        this._default = _default;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description
     *            the description to set
     */
    public void addDescription(WdkModelText description) {
        this.descriptions.add(description);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) throws WdkModelException {
        // exclude descriptions
        boolean hasDescription = false;
        for (WdkModelText description : descriptions) {
            if (description.include(projectId)) {
                if (hasDescription) {
                    throw new WdkModelException("The view " + getName()
                            + " has more than one description for project "
                            + projectId);
                } else {
                    this.description = description.getText();
                    hasDescription = true;
                }
            }
        }
        descriptions = null;
    }
}
