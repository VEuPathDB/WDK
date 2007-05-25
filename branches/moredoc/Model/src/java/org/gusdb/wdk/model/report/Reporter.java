/**
 * 
 */
package org.gusdb.wdk.model.report;

import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.wdk.model.Answer;
import org.gusdb.wdk.model.WdkModelException;

/**
 * @author xingao
 * 
 */
public abstract class Reporter {
    
    public static final String FIELD_FORMAT = "downloadType";

    protected Map< String, String > config;
    protected Answer answer;
    
    protected String format = "plain";

    protected Reporter( Answer answer ) {
        this.answer = answer;
        config = new LinkedHashMap< String, String >();
    }
    
    public void configure( Map< String, String > config ) {
        if ( config != null ) {
            this.config = config;

            if ( config.containsKey( FIELD_FORMAT ) ) {
                format = config.get( FIELD_FORMAT );
            }
        }
    }
    
    public String getHttpContentType() {
        // by default, generate result in plain text format
        return "text/plain";
    }
    
    public String getDownloadFileName() {
        // by default, display the result in the browser, by seting the file name as null
        return null;
    }
    
    public abstract void write( OutputStream out ) throws WdkModelException;
}
