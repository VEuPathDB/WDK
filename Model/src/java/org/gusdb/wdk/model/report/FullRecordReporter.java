/**
 * 
 */
package org.gusdb.wdk.model.report;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.Region;
import org.gusdb.wdk.model.Answer;
import org.gusdb.wdk.model.AttributeField;
import org.gusdb.wdk.model.AttributeFieldValue;
import org.gusdb.wdk.model.Field;
import org.gusdb.wdk.model.LinkValue;
import org.gusdb.wdk.model.RecordInstance;
import org.gusdb.wdk.model.TableField;
import org.gusdb.wdk.model.TableFieldValue;
import org.gusdb.wdk.model.WdkModelException;

/**
 * @author xingao
 * 
 */
public class FullRecordReporter extends Reporter {
    
    private static Logger logger = Logger.getLogger( TabularReporter.class );
    
    public static final String FIELD_SELECTED_COLUMNS = "selectedFields";
    public static final String FIELD_HAS_EMPTY_TABLE = "hasEmptyTable";
    
    private boolean hasEmptyTable = false;
    
    public FullRecordReporter( Answer answer ) {
        super( answer );
    }
    
    /*
     * 
     */
    @Override
    public void configure( Map< String, String > config ) {
        super.configure( config );
        
        // get basic configurations
        if ( config.containsKey( FIELD_HAS_EMPTY_TABLE ) ) {
            String value = config.get( FIELD_HAS_EMPTY_TABLE );
            hasEmptyTable = ( value.equalsIgnoreCase( "yes" ) || value.equalsIgnoreCase( "true" ) ) ? true
                    : false;
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
            return "appilication/vnd.ms-excel";
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
            return name + "_detail.txt";
        } else if ( format.equalsIgnoreCase( "excel" ) ) {
            return name + "_detail.xls";
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
        if ( format.equalsIgnoreCase( "excel" ) ) {
            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet( answer.getQuestion().getDisplayName() );
            formatRecord2Excel( attributes, tables, answer, sheet );
            try {
                workbook.write( out );
                out.flush();
            } catch ( IOException ex ) {
                throw new WdkModelException( ex );
            }
        } else {
            PrintWriter writer = new PrintWriter( new OutputStreamWriter( out ) );
            formatRecord2Text( attributes, tables, answer, writer );
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
    
    private void formatRecord2Text( Set< AttributeField > attributes,
            Set< TableField > tables, Answer answer, PrintWriter writer )
            throws WdkModelException {
        while ( answer.hasMoreRecordInstances() ) {
            RecordInstance record = answer.getNextRecordInstance();
            // print out attributes of the record first
            for ( AttributeField attribute : attributes ) {
                Object value = record.getAttributeValue( attribute );
                writer.println( attribute.getDisplayName() + ": " + value );
            }
            writer.println();
            writer.flush();
            
            // print out tables of the record
            for ( TableField table : tables ) {
                TableFieldValue tableValue = record.getTableValue( table.getName() );
                Iterator rows = tableValue.getRows();
                
                // check if table is empty
                if ( !hasEmptyTable && !rows.hasNext() ) {
                    tableValue.getClose();
                    continue;
                }
                
                AttributeField[ ] fields = table.getReportMakerFields();
                
                // output table header
                writer.println( "Table: " + table.getDisplayName() );
                for ( AttributeField attribute : fields ) {
                    writer.print( "[" + attribute.getDisplayName() + "]\t" );
                }
                writer.println();
                writer.flush();
                
                while ( rows.hasNext() ) {
                    Map rowMap = ( Map ) rows.next();
                    Iterator colNames = rowMap.keySet().iterator();
                    while ( colNames.hasNext() ) {
                        String colName = ( String ) colNames.next();
                        Object fVal = rowMap.get( colName );
                        // depending on the types of the object, print out the
                        // value of it
                        if ( fVal == null ) {
                            fVal = "";
                        } else if ( fVal instanceof AttributeFieldValue ) {
                            fVal = ( ( AttributeFieldValue ) fVal ).getValue();
                        } else if ( fVal instanceof LinkValue ) {
                            fVal = ( ( LinkValue ) fVal ).getVisible();
                        }
                        writer.print( fVal.toString() + "\t" );
                    }
                    writer.println();
                }
                tableValue.getClose();
                writer.println();
                writer.flush();
            }
            writer.println();
            writer.println( "------------------------------------------------------------" );
            writer.println();
            writer.flush();
        }
    }
    
    private void formatRecord2Excel( Set< AttributeField > attributes,
            Set< TableField > tables, Answer answer, HSSFSheet sheet )
            throws WdkModelException {
        int rowIndex = 0;
        while ( answer.hasMoreRecordInstances() ) {
            RecordInstance record = answer.getNextRecordInstance();
            // print out attributes of the record first
            for ( AttributeField attribute : attributes ) {
                Object value = record.getAttributeValue( attribute );
                if ( value == null ) value = "";
                HSSFRow row = sheet.createRow( rowIndex++ );
                short index = 0;
                row.createCell( index++ ).setCellValue(
                        attribute.getDisplayName() );
                row.createCell( index++ ).setCellValue( value.toString() );
            }
            sheet.createRow( rowIndex++ );
            
            // print out tables of the record
            for ( TableField table : tables ) {
                TableFieldValue tableValue = record.getTableValue( table.getName() );
                Iterator rows = tableValue.getRows();
                
                // check if table is empty
                if ( !hasEmptyTable && !rows.hasNext() ) {
                    tableValue.getClose();
                    continue;
                }
                
                AttributeField[ ] fields = table.getReportMakerFields();
                
                // output table header
                short index = 0;
                HSSFRow row = sheet.createRow( rowIndex++ );
                row.createCell( index++ ).setCellValue( "TABLE" );
                row.createCell( index++ ).setCellValue( table.getDisplayName() );
                row = sheet.createRow( rowIndex++ );
                index = 0;
                for ( AttributeField attribute : fields ) {
                    row.createCell( index++ ).setCellValue(
                            "[" + attribute.getDisplayName() + "]" );
                }
                
                while ( rows.hasNext() ) {
                    Map rowMap = ( Map ) rows.next();
                    row = sheet.createRow( rowIndex++ );
                    index = 0;
                    Iterator colNames = rowMap.keySet().iterator();
                    while ( colNames.hasNext() ) {
                        String colName = ( String ) colNames.next();
                        Object fVal = rowMap.get( colName );
                        // depending on the types of the object, print out the
                        // value of it
                        if ( fVal == null ) {
                            fVal = "";
                        } else if ( fVal instanceof AttributeFieldValue ) {
                            fVal = ( ( AttributeFieldValue ) fVal ).getValue();
                        } else if ( fVal instanceof LinkValue ) {
                            fVal = ( ( LinkValue ) fVal ).getVisible();
                        }
                        row.createCell( index++ ).setCellValue( fVal.toString() );
                    }
                }
                tableValue.getClose();
                sheet.createRow( rowIndex++ );
            }
            sheet.createRow( rowIndex++ );
            sheet.createRow( rowIndex++ ).createCell( ( short ) 0 ).setCellValue(
                    "------------------------------------------------------------" );
            int row = rowIndex - 1;
            sheet.addMergedRegion( new Region( row, ( short ) 0, row,
                    ( short ) 4 ) );
            sheet.createRow( rowIndex++ );
        }
    }
}
