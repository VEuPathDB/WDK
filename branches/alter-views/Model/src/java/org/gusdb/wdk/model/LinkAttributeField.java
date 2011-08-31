package org.gusdb.wdk.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class LinkAttributeField extends AttributeField {

    private List<WdkModelText> urls;
    private String url;

    private List<WdkModelText> displayTexts;
    private String displayText;

    public LinkAttributeField() {
        displayTexts = new ArrayList<WdkModelText>();
        urls = new ArrayList<WdkModelText>();
        // by default, don't show linked attributes in the download
        this.inReportMaker = false;
    }

    public void addUrl(WdkModelText url) {
        this.urls.add(url);
    }

    String getUrl() {
        return url;
    }

    /**
     * @return the displayText
     */
    public String getDisplayText() {
        return displayText;
    }

    /**
     * @param displayText
     *                the displayText to set
     */
    public void addDisplayText(WdkModelText displayText) {
        this.displayTexts.add(displayText);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) throws WdkModelException {
        super.excludeResources(projectId);
        
        String rcName = (recordClass == null) ? ""
                : (recordClass.getFullName() + ".");

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
        // check if all urls are excluded
        if (!hasUrl)
            throw new WdkModelException("The linkAttribute " + rcName + name
                    + " does not have a <url> tag for project " + projectId);
        urls = null;

        // exclude display texts
        boolean hasText = false;
        for (WdkModelText text : displayTexts) {
            if (text.include(projectId)) {
                if (hasText) {
                    throw new WdkModelException("The linkAttribute " + rcName
                            + getName() + " has more than one <displayText> "
                            + "for project " + projectId);
                } else {
                    this.displayText = text.getText();
                    hasText = true;
                }
            }
        }
        // check if all texts are excluded
        if (!hasText)
            throw new WdkModelException("The linkAttribute " + rcName + name
                    + " does not have a <displayText> tag for project "
                    + projectId);
        displayTexts = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.AttributeField#getDependents()
     */
    @Override
    protected Collection<AttributeField> getDependents()
            throws WdkModelException {
        Map<String, AttributeField> dependents = parseFields(url);
        dependents.putAll(parseFields(displayText));
        return dependents.values();
    }
}
