package org.gusdb.wdk.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TextAttributeField extends AttributeField {

    private List<WdkModelText> texts;
    /**
     * The text are used in the download, and should not include any html tags.
     */
    private String text;

    private List<WdkModelText> displays;
    /**
     * the display are used on the website, and html tags are allowed. display
     * is optional, and if not set, text will be used instead.
     */
    private String display;

    public TextAttributeField() {
        texts = new ArrayList<WdkModelText>();
        displays = new ArrayList<WdkModelText>();
    }

    public void addText(WdkModelText text) {
        this.texts.add(text);
    }

    public String getText() {
        return text;
    }

    public void addDisplay(WdkModelText display) {
        this.displays.add(display);
    }

    public String getDisplay() {
        return (display != null) ? display : text;
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

        // exclude texts
        boolean hasText = false;
        for (WdkModelText text : texts) {
            if (text.include(projectId)) {
                if (hasText) {
                    throw new WdkModelException("The textAttribute " + rcName
                            + getName() + " has more than one <text> for "
                            + "project " + projectId);
                } else {
                    this.text = text.getText();
                    hasText = true;
                }
            }
        }
        // check if all texts are excluded
        if (this.text == null)
            throw new WdkModelException("The text attribute " + rcName
                    + getName() + " does not have a <text> tag for project "
                    + projectId);
        texts = null;

        // exclude display, display is optional
        boolean hasDisplay = false;
        for (WdkModelText display : displays) {
            if (display.include(projectId)) {
                if (hasDisplay) {
                    throw new WdkModelException("The textAttribute " + rcName
                            + getName() + " has more than one <display> for "
                            + "project " + projectId);
                } else {
                    this.display = display.getText();
                    hasDisplay = true;
                }
            }
        }
        displays = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.AttributeField#getDependents()
     */
    @Override
    public Collection<AttributeField> getDependents() throws WdkModelException {
        String content = text;
        if (display != null) content += "\n" + display;
        return parseFields(content).values();
    }
}
