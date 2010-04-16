/**
 * 
 */
package org.gusdb.wdk.model.xml;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;

/**
 * @author Jerric
 * @created Oct 11, 2005
 */
public class XmlQuestion extends WdkModelBase {

    private String name;
    private String displayName;
    private String recordClassRef;
    private String xmlData;
    private String xsl;
    private String summaryAttributeNames;
    private XmlAttributeField[] summaryAttributes;

    private List<WdkModelText> descriptions = new ArrayList<WdkModelText>();
    private String description;

    private List<WdkModelText> helps = new ArrayList<WdkModelText>();
    private String help;

    private XmlQuestionSet questionSet;
    private XmlRecordClass recordClass;
    private XmlDataLoader loader;
    private WdkModel model;

    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * @param description
     *                The description to set.
     */
    public void addDescription(WdkModelText description) {
        this.descriptions.add(description);
    }

    /**
     * @return Returns the displayName.
     */
    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * @param displayName
     *                The displayName to set.
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @return Returns the help.
     */
    public String getHelp() {
        return this.help;
    }

    /**
     * @param help
     *                The help to set.
     */
    public void addHelp(WdkModelText help) {
        this.helps.add(help);
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return this.name;
    }

    public String getFullName() {
        if (questionSet == null) return getName();
        else return questionSet.getName() + "." + getName();
    }

    /**
     * @param name
     *                The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param summaryAttributesRef
     *                The summaryAttributesRef to set.
     */
    public void setSummaryAttributes(String summaryAttributeNames) {
        this.summaryAttributeNames = summaryAttributeNames;
    }

    public XmlAttributeField[] getSummaryAttributes() {
        return summaryAttributes;
    }

    /**
     * @param xmlData
     */
    public void setXmlDataURL(String xmlData) {
        this.xmlData = xmlData;
    }

    public String getXmlDataURL() {
        return xmlData;
    }

    public String getXslURL() {
        return xsl;
    }

    public void setXslURL(String xsl) {
        this.xsl = xsl;
    }

    public XmlQuestionSet getQuestionSet() {
        return questionSet;
    }

    public void setQuestionSet(XmlQuestionSet questionSet) {
        this.questionSet = questionSet;
    }

    /**
     * @return Returns the recordClass.
     */
    public XmlRecordClass getRecordClass() {
        return this.recordClass;
    }

    /*
     * <sanityXmlQuestion ref="XmlQuestions.News" pageStart="1" pageEnd="20"
     * minOutputLength="1" maxOutputLength="100"/>
     */
    public String getSanityTestSuggestion() throws WdkModelException {
        String indent = "    ";
        String newline = System.getProperty("line.separator");
        StringBuffer buf =
                new StringBuffer(newline + newline + indent
                        + "<sanityXmlQuestion ref=\"" + getFullName() + "\""
                        + newline + indent + indent + indent
                        + "minOutputLength=\"FIX_m_i_len\" "
                        + "maxOutputLength=\"FIX_m_o_len\"" + newline + indent
                        + indent + indent + "pageStart=\"1\" pageEnd=\"20\">"
                        + newline);
        buf.append(indent + "</sanityXmlQuestion>");
        return buf.toString();
    }

    /**
     * @param recordClassRef
     *                The recordClassRef to set.
     */
    public void setRecordClassRef(String recordClassRef) {
        this.recordClassRef = recordClassRef;
    }

    public void resolveReferences(WdkModel model) throws WdkModelException {
        // resolve the reference to XmlRecordClass
        recordClass = (XmlRecordClass) model.resolveReference(recordClassRef);

        // resolve the references to summary attributes
        if (summaryAttributeNames == null) { // default use all attribute
            // fields
            summaryAttributes = recordClass.getAttributeFields();
        } else { // use a subset of attribute fields
            Map<String, XmlAttributeField> summaries =
                    new LinkedHashMap<String, XmlAttributeField>();
            String[] names = summaryAttributeNames.split(",");
            for (String name : names) {
                try {
                    XmlAttributeField field =
                            recordClass.getAttributeField(name);
                    summaries.put(field.getName(), field);
                } catch (WdkModelException ex) {
                    // TODO Auto-generated catch block
                    ex.printStackTrace();
                    // System.err.println(ex);
                }
            }
            summaryAttributes = new XmlAttributeField[summaries.size()];
            summaries.values().toArray(summaryAttributes);
        }
    }

    public void setResources(WdkModel model) {
        // initialize data loader
        loader = new XmlDataLoader(model.getXmlSchemaURL());
        this.model = model;
    }

    public XmlAnswerValue makeAnswer(Map<String, String> params, int startIndex,
            int endIndex) throws WdkModelException {
        XmlAnswerValue answer;
        InputStream inXmlStream = null;
        InputStream inXslStream = null;
        ByteArrayOutputStream outXmlStream = null;
        InputStream convertedStream = null;
        try {
            URL xmlDataURL = createURL(xmlData);

            // check if we have the XSL assigned
            if (xsl != null && xsl.length() != 0) {
                // yes, convert the xml first
                URL xslURL = createURL(xsl);
                inXmlStream = xmlDataURL.openStream();
                inXslStream = xslURL.openStream();

                outXmlStream = new ByteArrayOutputStream();

                XmlConverter.convert(inXmlStream, inXslStream, outXmlStream,
                        name);

                byte[] buffer = outXmlStream.toByteArray();

                // TEST
                // System.out.println(new String(buffer));

                convertedStream = new ByteArrayInputStream(buffer);

                answer = loader.parseDataStream(convertedStream);
            } else { // no, just parse the xml directly
                answer = loader.parseDataFile(xmlDataURL);
            }
        } catch (MalformedURLException ex) {
            throw new WdkModelException(ex);
        } catch (IOException ex) {
            throw new WdkModelException(ex);
        } finally {
            try {
                if (inXmlStream != null) inXmlStream.close();
                if (inXslStream != null) inXslStream.close();
                if (outXmlStream != null) outXmlStream.close();
                if (convertedStream != null) convertedStream.close();
            } catch (IOException ex) {
                throw new WdkModelException(ex);
            }
        }
        // assign start & end index
        answer.setStartIndex((startIndex <= endIndex) ? startIndex : endIndex);
        answer.setEndIndex((startIndex <= endIndex) ? endIndex : startIndex);
        answer.setQuestion(this);
        answer.resolveReferences(this.model);
        answer.setResources(this.model);

        return answer;
    }

    private URL createURL(String data) throws MalformedURLException {
        if (data.startsWith("http://") || data.startsWith("ftp://")
                || data.startsWith("https://")) {
            return new URL(data);
        } else {
            File xmlDataDir = model.getXmlDataDir();
            File xmlDataFile = new File(xmlDataDir, data);
            return xmlDataFile.toURI().toURL();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer("XmlQuestion: name='");
        buf.append(name);
        buf.append("'\r\n\trecordClass='");
        buf.append(recordClassRef);
        buf.append("'\r\n\txmlDataURL='");
        buf.append(xmlData);
        buf.append("'\r\n\tdisplayName='");
        buf.append(getDisplayName());
        buf.append("'\r\n\tdescription='");
        buf.append(getDescription());
        buf.append("'\r\n\thelp='");
        buf.append(getHelp());
        buf.append("'\r\n");
        return buf.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) throws WdkModelException {
        // exclude the descriptions
        boolean hasDescription = false;
        for (WdkModelText description : descriptions) {
            if (description.include(projectId)) {
                if (hasDescription) {
                    throw new WdkModelException("The xmlQuestion "
                            + getFullName() + " has more than one description "
                            + "for project " + projectId);
                } else {
                    this.description = description.getText();
                    hasDescription = true;
                }
            }
        }
        descriptions = null;

        // exclude the helps
        boolean hasHelp = false;
        for (WdkModelText help : helps) {
            if (help.include(projectId)) {
                if (hasHelp) {
                    throw new WdkModelException("The xmlQuestion "
                            + getFullName() + " has more than one help "
                            + "for project " + projectId);
                } else {
                    this.help = help.getText();
                    hasHelp = true;
                }
            }
        }
        helps = null;
    }
}
