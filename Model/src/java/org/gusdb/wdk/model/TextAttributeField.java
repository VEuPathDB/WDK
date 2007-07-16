package org.gusdb.wdk.model;

import java.util.ArrayList;
import java.util.List;

public class TextAttributeField extends AttributeField {

    private List<WdkModelText> texts;
    private String text;

    public TextAttributeField() {
        texts = new ArrayList<WdkModelText>();
    }

    public void addText(WdkModelText text) {
        this.texts.add(text);
    }

    String getText() {
        return text;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) {
        // exclude texts
        for (WdkModelText text : texts) {
            if (text.include(projectId)) {
                this.text = text.getText();
                break;
            }
        }
        texts = null;
    }
}
