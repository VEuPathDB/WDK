/**
 * 
 */
package org.gusdb.wdk.model.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;

/**
 * @author Jerric
 * @created Oct 11, 2005
 */
public class XmlAnswer {

    private int startIndex;
    private int endIndex;
    private XmlQuestion question;
    private List<XmlRecordInstance> recordInstances;

    /**
     * 
     */
    public XmlAnswer() {
        recordInstances = new ArrayList<XmlRecordInstance>();
    }

    /**
     * @return Returns the endIndex.
     */
    public int getEndIndex() {
        return this.endIndex;
    }

    /**
     * @param endIndex The endIndex to set.
     */
    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

    /**
     * @return Returns the startIndex.
     */
    public int getStartIndex() {
        return this.startIndex;
    }

    /**
     * @param startIndex The startIndex to set.
     */
    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    /**
     * @return Returns the question.
     */
    public XmlQuestion getQuestion() {
        return this.question;
    }

    public void setQuestion(XmlQuestion question) {
        this.question = question;
    }

    public int getResultSize() {
        return recordInstances.size();
    }

    public int getPageSize() {
        int end = Math.min(endIndex, recordInstances.size());
        return (end - startIndex + 1);
    }

    public void addRecordInstance(XmlRecordInstance record) {
        this.recordInstances.add(record);
    }

    public XmlRecordInstance[] getRecordInstances() {
        XmlRecordInstance[] records = new XmlRecordInstance[recordInstances.size()];
        recordInstances.toArray(records);
        return records;
    }

    public void resolveReferences(WdkModel model) throws WdkModelException {
        // resolve references for record instances
        for (XmlRecordInstance record : recordInstances) {
            // set record class for the record, who will be used in the record's
            // resolveReferences() method.
            record.setRecordClass(question.getRecordClass());
            record.resolveReferences(model);
        }
    }

    public void setResources(WdkModel model) throws WdkModelException {
        // create summary attribute index
        XmlAttributeField[] sumArray = question.getSummaryAttributes();
        Map<String, XmlAttributeField> summaries = new HashMap<String, XmlAttributeField>();
        for (XmlAttributeField field : sumArray) {
            summaries.put(field.getName(), field);
        }

        // set record class for each instance
        for (XmlRecordInstance record : recordInstances) {
            // set summaryAttribute
            XmlAttributeValue[] attributes = record.getAttributes();
            for (XmlAttributeValue attribute : attributes) {
                attribute.setSummary(summaries.containsKey(attribute.getName()));
            }
        }
    }

    /**
     * @return
     */
    public String print() {
        StringBuffer sb = new StringBuffer(this.getClass().getName());

        // TODO - print question information
        sb.append(":\r\n\tname = ");
        sb.append(question.getName());
        sb.append(":\r\n\tdisplay name = ");
        sb.append(question.getDisplayName());
        sb.append(":\r\n\tdescription = ");
        sb.append(question.getDescription());
        sb.append(":\r\n\tdate url = ");
        sb.append(question.getXmlDataURL());
        sb.append("\r\n");

        // print out records
        for (XmlRecordInstance record : recordInstances) {
            sb.append("\r\n ------- Record ");
            sb.append(record.getRecordID());
            sb.append(" -------\r\n");
            sb.append(record.print());
        }
        return sb.toString();
    }

    /*
     * (non-Javadoc) print out the records in tabular format
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        XmlAttributeField[] summaryAttributes = question.getSummaryAttributes();

        // print out the field names
        for (XmlAttributeField field : summaryAttributes) {
            sb.append(field.getName());
            sb.append("\t");
        }
        sb.append("\r\n");

        // print out the records
        for (XmlRecordInstance record : recordInstances) {
            sb.append(record.toString());
            sb.append("\r\n");
        }
        return sb.toString();
    }
}
