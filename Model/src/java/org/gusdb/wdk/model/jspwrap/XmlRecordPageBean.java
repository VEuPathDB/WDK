/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.xml.XmlRecordPage;
import org.gusdb.wdk.model.xml.XmlRecordInstance;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Jerric
 * @created Oct 19, 2005
 */
public class XmlRecordPageBean {

    private XmlRecordPage answer;

    /**
     * 
     */
    public XmlRecordPageBean(XmlRecordPage answer) {
        this.answer = answer;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlRecordPage#getEndIndex()
     */
    public int getEndIndex() {
        return this.answer.getEndIndex();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlRecordPage#getPageSize()
     */
    public int getPageSize() {
        return this.answer.getPageSize();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlRecordPage#getQuestion()
     */
    public XmlQuestionBean getQuestion() {
        return new XmlQuestionBean(answer.getQuestion());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlRecordPage#getRecordInstances()
     */
    public XmlRecordBean[] getRecordInstances() {
        XmlRecordInstance[] records = answer.getRecordInstances();
        XmlRecordBean[] recordBeans = new XmlRecordBean[records.length];
        for (int i = 0; i < records.length; i++) {
            recordBeans[i] = new XmlRecordBean(records[i]);
        }
        return recordBeans;
    }

    public Map<String, XmlRecordBean> getRecordInstanceMap() {
        Map<String, XmlRecordInstance> records = answer.getRecordInstanceMap();
        Map<String, XmlRecordBean> recordBeans = new LinkedHashMap<String, XmlRecordBean>(
                records.size());
        for (String recordId : records.keySet()) {
            recordBeans.put(recordId, new XmlRecordBean(records.get(recordId)));
        }
        return recordBeans;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlRecordPage#getResultSize()
     */
    public int getResultSize() {
        return this.answer.getResultSize();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlRecordPage#getStartIndex()
     */
    public int getStartIndex() {
        return this.answer.getStartIndex();
    }

    public XmlRecordClassBean getRecordClass() {
        return new XmlRecordClassBean(answer.getQuestion().getRecordClass());
    }
}
