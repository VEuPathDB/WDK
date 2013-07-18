/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.wdk.model.answer.AnswerFilterInstance;
import org.gusdb.wdk.model.answer.AnswerFilterLayout;

/**
 * @author SYSTEM
 * 
 */
public class AnswerFilterLayoutBean {

    private AnswerFilterLayout layout;

    AnswerFilterLayoutBean(AnswerFilterLayout layout) {
        this.layout = layout;
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.answer.AnswerFilterLayout#getDescription()
     */
    public String getDescription() {
        return layout.getDescription();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.answer.AnswerFilterLayout#getDisplayName()
     */
    public String getDisplayName() {
        return layout.getDisplayName();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.answer.AnswerFilterLayout#getName()
     */
    public String getName() {
        return layout.getName();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.answer.AnswerFilterLayout#getInstanceMap()
     */
    public Map<String, AnswerFilterInstanceBean> getInstanceMap() {
        Map<String, AnswerFilterInstanceBean> beanMap = new LinkedHashMap<String, AnswerFilterInstanceBean>();
        Map<String, AnswerFilterInstance> instanceMap = layout.getInstanceMap();
        for (String name : instanceMap.keySet()) {
            AnswerFilterInstance instance = instanceMap.get(name);
            beanMap.put(name, new AnswerFilterInstanceBean(instance));
        }
        return beanMap;
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.answer.AnswerFilterLayout#getInstances()
     */
    public AnswerFilterInstanceBean[] getInstances() {
        AnswerFilterInstance[] instances = layout.getInstances();
        AnswerFilterInstanceBean[] beans = new AnswerFilterInstanceBean[instances.length];
        for (int i = 0; i < instances.length; i++) {
            beans[i] = new AnswerFilterInstanceBean(instances[i]);
        }
        return beans;
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.answer.AnswerFilterLayout#getlayoutMap()
     */
    public String getFileName() {
        return layout.getFileName();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.answer.AnswerFilterLayout#isVisible()
     */
    public boolean isVisible() {
        return layout.isVisible();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.answer.AnswerFilterLayout#isVertical()
     */
    public boolean isVertical() {
        return layout.isVertical();
    }

}
