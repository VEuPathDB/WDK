/**
 * 
 */
package org.gusdb.wdk.model.report;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.gusdb.wdk.model.Answer;
import org.gusdb.wdk.model.AttributeField;
import org.gusdb.wdk.model.Field;
import org.gusdb.wdk.model.RecordInstance;
import org.gusdb.wdk.model.TableField;
import org.gusdb.wdk.model.TableFieldValue;
import org.gusdb.wdk.model.WdkModelException;

/**
 * @author xingao
 * 
 */
public class FullRecordReporter extends Reporter {
    
    private static final String FIELD_SELECTED_COLUMNS = "selectedFields";
    
    public FullRecordReporter( Answer answer ) {
        super( answer );
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.report.IReporter#format(org.gusdb.wdk.model.Answer)
     */
    public void write( OutputStream out ) throws WdkModelException {
        // get the columns that will be in the report
        Set< Field > fields = validateColumns( answer );
        Set< AttributeField > attributes = new LinkedHashSet< AttributeField >();
        Set< TableField > tables = new LinkedHashSet< TableField >();
        for ( Field field : fields ) {
            if ( field instanceof AttributeField ) {
                attributes.add( ( AttributeField ) field );
            } else if ( field instanceof TableField ) {
                tables.add( ( TableField ) field );
            }
        }
        
        // get the formatted result
        PrintWriter writer = new PrintWriter( new OutputStreamWriter( out ) );
        while ( answer.hasMoreRecordInstances() ) {
            RecordInstance record = answer.getNextRecordInstance();
            formatRecord( attributes, tables, record, writer );
            writer.println( "---------------------" );
            writer.flush();
        }
    }
    
    private Set< Field > validateColumns( Answer answer )
            throws WdkModelException {
        // get a map of report maker fields
        Map< String, Field > fieldMap = answer.getQuestion().getReportMakerFields();
        
        // the config map contains a list of column names;
        Set< Field > columns = new LinkedHashSet< Field >();
        
        String fieldsList = config.get( FIELD_SELECTED_COLUMNS );
        if ( fieldsList == null ) {
            columns.addAll( fieldMap.values() );
        } else {
            String[ ] fields = fieldsList.split( "," );
            for ( String column : fields ) {
                column = column.trim();
                if ( !fieldMap.containsKey( column ) )
                    throw new WdkModelException( "The column '" + column
                            + "' cannot be included in the report" );
                columns.add( fieldMap.get( column ) );
            }
        }
        return columns;
    }
    
    private void formatRecord( Set< AttributeField > attributes,
            Set< TableField > tables, RecordInstance record, PrintWriter writer )
            throws WdkModelException {
        // print out attributes of the record first
        for ( AttributeField attribute : attributes ) {
            Object value = record.getAttributeValue( attribute );
            writer.println( attribute.getDisplayName() + ":" + value );
        }
        
        // print out tables of the record
        for ( TableField table : tables ) {
            TableFieldValue value = record.getTableValue( table.getName() );
            writer.println();
            writer.println( "<Table> " + table.getDisplayName() );
            StringBuffer buffer = new StringBuffer();
            value.write( buffer );
            writer.println( buffer );
        }
    }
}
