package org.gusdb.wdk.model.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.xml.TransformException;
import org.gusdb.fgputil.xml.XmlTransformer;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;

/**
 * @author Jerric
 */
public class XmlQuestion extends WdkModelBase {

    private String _name;
    private String _displayName;
    private String _recordClassRef;
    private String _xmlData;
    private String _xsl;
    private String _summaryAttributeNames;
    private XmlAttributeField[] _summaryAttributes;

    private List<WdkModelText> _descriptions = new ArrayList<WdkModelText>();
    private String _description;

    private List<WdkModelText> _helps = new ArrayList<WdkModelText>();
    private String _help;

    private XmlQuestionSet _questionSet;
    private XmlRecordClass _recordClass;
    private XmlDataLoader _loader;
    private WdkModel _model;
    
    private static final Logger LOG = Logger.getLogger(XmlQuestion.class);

    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return _description;
    }

    /**
     * @param description
     *                The description to set.
     */
    public void addDescription(WdkModelText description) {
        _descriptions.add(description);
    }

    /**
     * @return Returns the displayName.
     */
    public String getDisplayName() {
        return _displayName;
    }

    /**
     * @param displayName
     *                The displayName to set.
     */
    public void setDisplayName(String displayName) {
        _displayName = displayName;
    }

    /**
     * @return Returns the help.
     */
    public String getHelp() {
        return _help;
    }

    /**
     * @param help
     *                The help to set.
     */
    public void addHelp(WdkModelText help) {
        _helps.add(help);
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return _name;
    }

    public String getFullName() {
        if (_questionSet == null) return getName();
        else return _questionSet.getName() + "." + getName();
    }

    /**
     * @param name
     *                The name to set.
     */
    public void setName(String name) {
        _name = name;
    }

    /**
     * @param summaryAttributeNames
     *                The summaryAttributes to set.
     */
    public void setSummaryAttributes(String summaryAttributeNames) {
        _summaryAttributeNames = summaryAttributeNames;
    }

    public XmlAttributeField[] getSummaryAttributes() {
        return _summaryAttributes;
    }

    /**
     * @param xmlData
     */
    public void setXmlDataURL(String xmlData) {
        _xmlData = xmlData;
    }

    public String getXmlDataURL() {
        return _xmlData;
    }

    public String getXslURL() {
        return _xsl;
    }

    public void setXslURL(String xsl) {
        _xsl = xsl;
    }

    public XmlQuestionSet getQuestionSet() {
        return _questionSet;
    }

    public void setQuestionSet(XmlQuestionSet questionSet) {
        _questionSet = questionSet;
    }

    /**
     * @return Returns the recordClass.
     */
    public XmlRecordClass getRecordClass() {
        return _recordClass;
    }

    /*
     * <sanityXmlQuestion ref="XmlQuestions.News" pageStart="1" pageEnd="20"
     * minOutputLength="1" maxOutputLength="100"/>
     */
    public String getSanityTestSuggestion() {
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
        _recordClassRef = recordClassRef;
    }

    @Override
    public void resolveReferences(WdkModel model) throws WdkModelException {
        // resolve the reference to XmlRecordClass
        _recordClass = (XmlRecordClass) model.resolveReference(_recordClassRef);

        // resolve the references to summary attributes
        if (_summaryAttributeNames == null) { // default use all attribute
            // fields
            _summaryAttributes = _recordClass.getAttributeFields();
        } else { // use a subset of attribute fields
            Map<String, XmlAttributeField> summaries =
                    new LinkedHashMap<String, XmlAttributeField>();
            String[] names = _summaryAttributeNames.split(",");
            for (String name : names) {
                try {
                    XmlAttributeField field =
                            _recordClass.getAttributeField(name);
                    summaries.put(field.getName(), field);
                } catch (WdkModelException ex) {
                    // TODO Auto-generated catch block
                    ex.printStackTrace();
                    // System.err.println(ex);
                }
            }
            _summaryAttributes = new XmlAttributeField[summaries.size()];
            summaries.values().toArray(_summaryAttributes);
        }
    }

    public void setResources(WdkModel model) {
        // initialize data loader
        _loader = new XmlDataLoader(model.getXmlSchemaURL());
        _model = model;
    }

    public XmlAnswerValue makeAnswer(int startIndex, int endIndex) throws WdkModelException {
        XmlAnswerValue answer;
        InputStream inXmlStream = null;
        InputStream inXslStream = null;
        ByteArrayOutputStream outXmlStream = null;
        InputStream convertedStream = null;
        try {
            URL xmlDataURL = createURL(_xmlData);

            // check if we have the XSL assigned
            if (_xsl != null && _xsl.length() != 0) {
                // yes, convert the xml first
                URL xslURL = createURL(_xsl);
                inXmlStream = xmlDataURL.openStream();
                inXslStream = xslURL.openStream();

                outXmlStream = new ByteArrayOutputStream();

                XmlTransformer.convert(inXmlStream, inXslStream, outXmlStream, _name);

                byte[] buffer = outXmlStream.toByteArray();

                // TEST
                // System.out.println(new String(buffer));

                convertedStream = new ByteArrayInputStream(buffer);

                answer = _loader.parseDataStream(convertedStream);
            } else { // no, just parse the xml directly
                answer = _loader.parseDataFile(xmlDataURL);
            }
        } catch (IOException | TransformException ex) {
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
        answer.resolveReferences();
        answer.setResources();

        return answer;
    }

    private URL createURL(String data) throws MalformedURLException {
        if (data.startsWith("http://") || data.startsWith("ftp://")
                || data.startsWith("https://")) {
            return new URL(data);
        } else {
            File xmlDataDir = _model.getXmlDataDir();
            File xmlDataFile = new File(xmlDataDir, data);
            return xmlDataFile.toURI().toURL();
        }
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer("XmlQuestion: name='");
        buf.append(_name);
        buf.append("'\r\n\trecordClass='");
        buf.append(_recordClassRef);
        buf.append("'\r\n\txmlDataURL='");
        buf.append(_xmlData);
        buf.append("'\r\n\tdisplayName='");
        buf.append(getDisplayName());
        buf.append("'\r\n\tdescription='");
        buf.append(getDescription());
        buf.append("'\r\n\thelp='");
        buf.append(getHelp());
        buf.append("'\r\n");
        return buf.toString();
    }

    @Override
    public void excludeResources(String projectId) throws WdkModelException {
        // exclude the descriptions
        boolean hasDescription = false;
        for (WdkModelText description : _descriptions) {
            if (description.include(projectId)) {
                if (hasDescription) {
                    throw new WdkModelException("The xmlQuestion "
                            + getFullName() + " has more than one description "
                            + "for project " + projectId);
                } else {
                    _description = description.getText();
                    hasDescription = true;
                }
            }
        }
        _descriptions = null;

        // exclude the helps
        boolean hasHelp = false;
        for (WdkModelText help : _helps) {
            if (help.include(projectId)) {
                if (hasHelp) {
                    throw new WdkModelException("The xmlQuestion "
                            + getFullName() + " has more than one help "
                            + "for project " + projectId);
                } else {
                    _help = help.getText();
                    hasHelp = true;
                }
            }
        }
        _helps = null;
    }
    
  public XmlAnswerValue getFullAnswer() throws WdkModelException {
    try {
      XmlAnswerValue a = makeAnswer(1, 3);
      int c = a.getResultSize();
      return makeAnswer(1, c);
    }
    catch (WdkModelException ex) {
      LOG.error("Error on getting answer from xmlQuestion '" + getFullName() + "': " + ex);
      ex.printStackTrace();
      throw ex;
    }
  }
}
