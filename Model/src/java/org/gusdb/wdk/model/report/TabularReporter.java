/**
 * 
 */
package org.gusdb.wdk.model.report;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.gusdb.wdk.model.Answer;
import org.gusdb.wdk.model.AttributeField;
import org.gusdb.wdk.model.LinkValue;
import org.gusdb.wdk.model.RecordInstance;
import org.gusdb.wdk.model.WdkModelException;

/**
 * @author xingao
 * 
 */
public class TabularReporter extends Reporter {
    
    private static Logger logger = Logger.getLogger( TabularReporter.class );
    
    public static final String FIELD_HAS_HEADER = "includeHeader";
    public static final String FIELD_DIVIDER = "divider";
    public static final String FIELD_SELECTED_COLUMNS = "selectedFields";
    
    private boolean hasHeader = true;
    private String divider = "\t";
    
    public TabularReporter( Answer answer ) {
        super( answer );
    }
    
    /*
     * 
     */
    @Override
    public void configure( Map< String, String > config ) {
        super.configure( config );
        
        // get basic configurations
        if ( config.containsKey( FIELD_HAS_HEADER ) ) {
            String value = config.get( FIELD_HAS_HEADER );
            hasHeader = ( value.equalsIgnoreCase( "yes" ) || value.equalsIgnoreCase( "true" ) ) ? true
                    : false;
        }
        
        if ( config.containsKey( FIELD_DIVIDER ) ) {
            divider = config.get( FIELD_DIVIDER );
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.report.Reporter#getHttpContentType()
     */
    @Override
    public String getHttpContentType() {
        if ( format.equalsIgnoreCase( "text" ) ) {
            return "text/plain";
        } else if ( format.equalsIgnoreCase( "excel" ) ) {
            return "application/vnd.ms-excel";
        } else { // use the default content type defined in the parent class
            return super.getHttpContentType();
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.report.Reporter#getDownloadFileName()
     */
    @Override
    public String getDownloadFileName() {
        logger.info( "Internal format: " + format );
        String name = answer.getQuestion().getName();
        if ( format.equalsIgnoreCase( "text" ) ) {
            return name + "_summary.txt";
        } else if ( format.equalsIgnoreCase( "excel" ) ) {
            return name + "_summary.xls";
        } else { // use the defaul file name defined in the parent
            return super.getDownloadFileName();
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.report.IReporter#format(org.gusdb.wdk.model.Answer)
     */
    public void write( OutputStream out ) throws WdkModelException {
        
        // get the columns that will be in the report
        Set< AttributeField > columns = validateColumns( answer );
        
        // get the formatted result
        if ( format.equalsIgnoreCase( "excel" ) ) {
            format2Excel( columns, answer, out );
        } else {
            format2Text( columns, answer, out );
        }
    }
    
    private Set< AttributeField > validateColumns( Answer answer )
            throws WdkModelException {
        // the config map contains a list of column names;
        Map< String, AttributeField > summary = answer.getSummaryAttributes();
        Set< AttributeField > columns = new LinkedHashSet< AttributeField >();
        
        String fieldsList = config.get( FIELD_SELECTED_COLUMNS );
        if ( fieldsList == null ) {
            columns.addAll( summary.values() );
        } else {
            Map< String, AttributeField > attributes = answer.getQuestion().getReportMakerAttributeFields();
            String[ ] fields = fieldsList.split( "," );
            for ( String column : fields ) {
                column = column.trim();
                if ( column.equalsIgnoreCase( "default" ) ) {
                    columns.clear();
                    columns.addAll( summary.values() );
                    break;
                }
                if ( !attributes.containsKey( column ) )
                    throw new WdkModelException( "The column '" + column
                            + "' cannot included in the report" );
                columns.add( attributes.get( column ) );
            }
        }
        return columns;
    }
    
    private void format2Text( Set< AttributeField > columns, Answer answer,
            OutputStream out ) throws WdkModelException {
        PrintWriter writer = new PrintWriter( new OutputStreamWriter( out ) );
        
        // print the header
        if ( hasHeader ) {
            for ( AttributeField column : columns ) {
                writer.print( "[" + column.getDisplayName() + "]" );
                writer.print( divider );
            }
            writer.println();
            writer.flush();
        }
        
        while ( answer.hasMoreRecordInstances() ) {
            RecordInstance record = answer.getNextRecordInstance();
            for ( AttributeField column : columns ) {
                Object value = record.getAttributeValue( column );
                if ( value instanceof LinkValue ) {
                    writer.print( ( ( LinkValue ) value ).getValue() );
                } else {
                    writer.print( value );
                }
                writer.print( divider );
            }
            writer.println();
            writer.flush();
        }
    }
    
    private void format2Excel( Set< AttributeField > columns, Answer answer,
            OutputStream out ) throws WdkModelException {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet( answer.getQuestion().getDisplayName() );
        
        int rowIndex = 0;
        if ( hasHeader ) {
            HSSFRow row = sheet.createRow( rowIndex++ );
            short index = 0;
            for ( AttributeField column : columns ) {
                row.createCell( index++ ).setCellValue(
                        "[" + column.getDisplayName() + "]" );
            }
        }
        
        while ( answer.hasMoreRecordInstances() ) {
            RecordInstance record = answer.getNextRecordInstance();
            HSSFRow row = sheet.createRow( rowIndex++ );
            short index = 0;
            for ( AttributeField column : columns ) {
                Object value = record.getAttributeValue( column );
                if ( value instanceof LinkValue ) {
                    row.createCell( index++ ).setCellValue(
                            ( ( LinkValue ) value ).getValue() );
                } else {
                    row.createCell( index++ ).setCellValue( value.toString() );
                }
            }
        }
        try {
            workbook.write( out );
            out.flush();
        } catch ( IOException ex ) {
            throw new WdkModelException( ex );
        }
    }
}
