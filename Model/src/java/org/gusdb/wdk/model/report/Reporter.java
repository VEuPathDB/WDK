/**
 * 
 */
package org.gusdb.wdk.model.report;

import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.Answer;
import org.gusdb.wdk.model.AttributeField;
import org.gusdb.wdk.model.FieldScope;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.json.JSONException;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * @author xingao
 * 
 */
public abstract class Reporter implements Iterable<Answer> {

    public static final String FIELD_FORMAT = "downloadType";

    private final static Logger logger = Logger.getLogger(Reporter.class);

    private class PageAnswerIterator implements Iterator<Answer> {

        private static final int MAX_PAGE_SIZE = 100;

        private Answer baseAnswer;
        private int endIndex;
        private int startIndex;

        PageAnswerIterator(Answer answer, int startIndex, int endIndex)
                throws WdkModelException, NoSuchAlgorithmException,
                SQLException, JSONException, WdkUserException {
            this.baseAnswer = answer;

            // determine the end index, which should be no bigger result size,
            // since the index starts from 1
            int resultSize = baseAnswer.getResultSize();
            this.endIndex = Math.min(endIndex, resultSize);

            this.startIndex = startIndex;
        }

        public boolean hasNext() {
            // if the current
            return (startIndex <= endIndex);
        }

        public Answer next() {
            // decide the new end index for the page answer
            int pageEndIndex = Math.min(endIndex, startIndex + MAX_PAGE_SIZE
                    - 1);

            logger.debug("Getting records #" + startIndex + " to #"
                    + pageEndIndex);

            Answer answer = new Answer(baseAnswer, startIndex, pageEndIndex);
            // update the current index
            startIndex = pageEndIndex + 1;
            return answer;
        }

        public void remove() {
            throw new NotImplementedException();
        }

    }

    public abstract void write(OutputStream out) throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException;

    protected Map<String, String> properties;
    protected Map<String, String> config;

    protected WdkModel wdkModel;

    protected Answer baseAnswer;
    private int startIndex;
    private int endIndex;

    protected String format = "plain";

    protected Reporter(Answer answer, int startIndex, int endIndex) {
        this.baseAnswer = answer;
        this.startIndex = startIndex;
        this.endIndex = endIndex;

        config = new LinkedHashMap<String, String>();
    }

    public void setProperties(Map<String, String> properties)
            throws WdkModelException {
        this.properties = properties;
    }

    public void configure(Map<String, String> config) {
        if (config != null) {
            this.config = config;

            if (config.containsKey(FIELD_FORMAT)) {
                format = config.get(FIELD_FORMAT);
            }
        }
    }

    public void setWdkModel(WdkModel wdkModel) {
        this.wdkModel = wdkModel;
    }

    public String getHttpContentType() {
        // by default, generate result in plain text format
        return "text/plain";
    }

    public String getDownloadFileName() {
        // by default, display the result in the browser, by seting the file
        // name as null
        return null;
    }

    // =========================================================================
    // provide the wrapper methods to answer object, in order not to expose the
    // answer itself to avoid accidental changes on the base answer. The record
    // access to the answer should be through the page answer iterator
    // =========================================================================

    /**
     * @return get the questions of the answer
     */
    protected Question getQuestion() {
        return baseAnswer.getQuestion();
    }

    /**
     * @return
     */
    protected Map<String, AttributeField> getSummaryAttributes() {
        return baseAnswer.getAttributeFieldMap(FieldScope.SUMMARY);
    }

    public Iterator<Answer> iterator() {
        try {
            return new PageAnswerIterator(baseAnswer, startIndex, endIndex);
        } catch (WdkModelException ex) {
            throw new RuntimeException(ex);
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        } catch (JSONException ex) {
            throw new RuntimeException(ex);
        } catch (WdkUserException ex) {
            throw new RuntimeException(ex);
        }
    }

}
