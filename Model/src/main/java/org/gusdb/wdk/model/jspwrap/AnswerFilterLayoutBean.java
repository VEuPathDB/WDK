/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

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
     * @see org.gusdb.wdk.model.answer.AnswerFilterLayout#getSortedInstanceMap()
     */
    public Map<String, AnswerFilterInstanceBean> getSortedInstanceMap() {
        Map<String, AnswerFilterInstanceBean> beanMap = new TreeMap<String, AnswerFilterInstanceBean>();
        Map<String, AnswerFilterInstance> sortedInstanceMap = layout.getSortedInstanceMap();
        for (String name : sortedInstanceMap.keySet()) {
            AnswerFilterInstance instance = sortedInstanceMap.get(name);
            beanMap.put(name, new AnswerFilterInstanceBean(instance));
        }
        return beanMap;
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.answer.AnswerFilterLayout#getSortedInstances()
     */
    public AnswerFilterInstanceBean[] getSortedInstances() {
        AnswerFilterInstance[] instances = layout.getSortedInstances();
        AnswerFilterInstanceBean[] beans = new AnswerFilterInstanceBean[instances.length];
        for (int i = 0; i < instances.length; i++) {
            beans[i] = new AnswerFilterInstanceBean(instances[i]);
        }
        return beans;
    }

 /**
     * @return
     * @see org.gusdb.wdk.model.answer.AnswerFilterLayout#getInstanceCountMap()
     */

    public Map<String, Integer> getInstanceCountMap() {
        Map<String, Integer> beanMap = new LinkedHashMap<String, Integer>();
        Map<String, Integer> instanceCountMap = layout.getInstanceCountMap();
        for (String name : instanceCountMap.keySet()) {
            Integer count = instanceCountMap.get(name);
            beanMap.put(name, new Integer(count));
        }
        return beanMap;
    }

 /**
     * @return
     * @see org.gusdb.wdk.model.answer.AnswerFilterLayout#getsortedFamilyCountMap()
     */

    public Map<String, Integer> getSortedFamilyCountMap() {
        Map<String, Integer> beanMap = new LinkedHashMap<String, Integer>();
        Map<String, Integer> sortedFamilyCountMap = layout.getSortedFamilyCountMap();
        for (String name : sortedFamilyCountMap.keySet()) {
            Integer count = sortedFamilyCountMap.get(name);
            beanMap.put(name, new Integer(count));
        }
        return beanMap;
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
