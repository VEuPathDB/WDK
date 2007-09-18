package org.gusdb.wdk.model;

import java.util.ArrayList;
import java.util.List;

public class LinkAttributeField extends AttributeField {

    private List<WdkModelText> urls;
    private String url;

    private String visible;

    public LinkAttributeField() {
        urls = new ArrayList<WdkModelText>();
    }

    public void setVisible(String visible) {
        this.visible = visible;
    }

    public void addUrl(WdkModelText url) {
        this.urls.add(url);
    }

    String getUrl() {
        return url;
    }

    String getVisible() {
        return visible;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) throws WdkModelException {
        String rcName =
                (recordClass == null) ? "" : (recordClass.getFullName() + ".");

        // exclude urls
        boolean hasUrl = false;
        for (WdkModelText url : urls) {
            if (url.include(projectId)) {
                if (hasUrl) {
                    throw new WdkModelException("The linkAttribute " + rcName
                            + getName() + " has more than one <url> for "
                            + "project " + projectId);
                } else {
                    this.url = url.getText();
                    hasUrl = true;
                }
            }
        }
        // check if all texts are excluded
        if (this.url == null)
            throw new WdkModelException("The linkAttribute " + rcName
                    + getName() + " does not have a <url> tag for project "
                    + projectId);
        urls = null;
    }
}
