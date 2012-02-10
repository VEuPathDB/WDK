package org.gusdb.wdk.view;

import java.util.ArrayList;
import java.util.List;

import org.gusdb.wdk.model.WdkModelBase;

public abstract class WdkView extends WdkModelBase {

    private String name;
    private String display;
    private String jsp;
    private boolean _default;


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

}
