package org.gusdb.wdk.model.answer.stream;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.gusdb.fgputil.IoUtil;
import org.gusdb.fgputil.Timer;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.dbms.SqlResultList;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.record.CsvResultList;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.record.TableField;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.ColumnAttributeField;
import org.gusdb.wdk.model.record.attribute.PrimaryKeyAttributeField;

import au.com.bytecode.opencsv.CSVWriter;

public class FileBasedRecordStream implements RecordStream {
	
  private static final Logger logger = Logger.getLogger(FileBasedRecordStream.class);
  
  /** Buffer size for the buffered writer use to write CSV files */
  private static final int BUFFER_SIZE = 32768;
  
  /** 
   * Suffix for temporary CSV files constructed from table queries.  Done in part
   * to avoid conflicts with column attribute queries with the same name.
   */
  private static final String TABLE_DESIGNATION = "_table";
  
  /** Extension applied to temporary CSV file names */
  private static final String TEMP_FILE_EXT = ".txt";
  
  /** The prefix to be applied to a temporary directory */
  private static final String DIRECTORY_PREFIX = "wdk_";

  private final AnswerValue _answerValue;
  private final Collection<AttributeField> _attributes;
  private final Collection<TableField> _tables;
  private final List<FileBasedRecordIterator> _iterators = new ArrayList<>();
  private Path _temporaryDirectory;
  private Map<Path, List<ColumnAttributeField>> _attributeFileMap = new HashMap<>();
  private Map<Path, TableField> _tableFileMap = new HashMap<>();
  private boolean _filesPopulated = false;

  /**
   * Creates a record stream that can provide all records without paging by
   * caching attribute and table query results in files and then reading
   * from those file in parallel to construct RecordInstance objects one by
   * one as requested by the provided iterator.
   * 
   * @param answerValue answer value defining the records to be returned
   * @param fileRepository file repository where temporary files will be written
   * @param attributes collection of requested attribute fields
   * @param tables collection of requested table fields
   */
  public FileBasedRecordStream(
      AnswerValue answerValue,
      Collection<AttributeField> attributes,
      Collection<TableField> tables) {
    _answerValue = answerValue;
    _attributes = attributes;
    _tables = tables;
  }

  /**
   * Serially executes all attribute and table queries required to construct
   * RecordInstance objects based on the attribute and table sets requested
   * in the constructor.  Handles opening and closing DB connections, serializing
   * results to files, and closing files.
   * 
   * @throws WdkModelException if unable to complete population
   * @throws WdkUserException 
   */
  public void populateFiles() throws WdkModelException, WdkUserException {
	
      createTemporaryDirectory();
      if(_attributes != null && _attributes.size() > 0) {
	    assembleAttributeFiles();
	  }
	  if(_tables != null && _tables.size() > 0) {
	    assembleTableFiles();
	  }
	  // temporary measure to allow deletion of this files manually.
	  // TODO Remove world write permission on production
	  makeFilesWorldWriteable();
      _filesPopulated = true;
  }
  
  /**
   * Create a temporary directory to house the temporary CSV file to be created.
   * @throws WdkModelException
   */
  protected void createTemporaryDirectory() throws WdkModelException {
	try {
	  String wdkTempDir = _answerValue.getQuestion().getWdkModel().getModelConfig().getWdkTempDir();
	  _temporaryDirectory = IoUtil.createOpenPermsTempDir(Paths.get(wdkTempDir), DIRECTORY_PREFIX);
	}
	catch(IOException ioe) {
      throw new WdkModelException(ioe.getMessage());
	}
  }

  /**
   * Provides handle to temporary directory housing temporary CSV files
   * @return - path to temporary directory
   */
  public Path getTemporaryDirectory() {
	return _temporaryDirectory;
  }
  
  /**
   * Some non-column attribute fields may depend on column attribute fields not provided
   * in the given attribute field list.  Those need to be added.  While were are at it, we can
   * just provide a column attribute field list to avoid having to check whether attributes
   * are in fact column attributes.
   * @param attributes - list of attribute fields of any flavor
   * @param tableAttributes - flag to indicate whether these attributes or from a table or not.
   * @return - subset of original attribute fields list containing only column attribute fields
   * @throws WdkModelException
   */
  protected static List<ColumnAttributeField> filterColumnAttributeFields(Collection<AttributeField> attributes, boolean tableAttributes) throws WdkModelException {
    List<ColumnAttributeField> columnAttributes = new ArrayList<>();
    for(AttributeField attribute : attributes) {
      if(attribute instanceof ColumnAttributeField) {
    	columnAttributes.add((ColumnAttributeField)attribute);
      }
      // Addition of underlying but unspecified column attributes does not apply to table attributes.
      if(!tableAttributes) {
        columnAttributes.addAll(attribute.getColumnAttributeFields().values());
      }
    }
    return columnAttributes;
  }
  
  /**
   * Culls all the column attribute fields from the table field and returns a list of primary key column names and
   * column attribute column names.
   * @param answerValue
   * @param table - the given table field
   * @return ordered list of primary key column names and table column attribute column names.
   * @throws WdkModelException
   */
  public static List<String> createTableColumnList(AnswerValue answerValue, TableField table) throws WdkModelException {
	  
    // Collect together all the columns representing the primary key attribute fields.
    PrimaryKeyAttributeField pkField = answerValue.getQuestion().getRecordClass().getPrimaryKeyAttributeField();
    String[] pkColumns = pkField.getColumnRefs();

    // Collect together all the columns representing the column attribute fields to be included
    List<String> attributeColumns = new ArrayList<>();
    List<ColumnAttributeField> fields = filterColumnAttributeFields(Arrays.asList(table.getAttributeFields()),true);
    for(ColumnAttributeField field : fields) {
  	  attributeColumns.add(field.getColumn().getName());
  	}

    // Combine the primary key columns and column attribute columns into a single list
    List<String> columnNames = new ArrayList<>(Arrays.asList(pkColumns));
    columnNames.addAll(attributeColumns);

    return columnNames;
  }

  /**
   * Collect all the queries needed to accommodate the requested attributes.  Using a set to avoid dups.
   * @param columnAttributes
   * @return a set of attribute queries
   * @throws WdkModelException
   */
  public Set<Query> getAttributeQueries(List<ColumnAttributeField> columnAttributes) throws WdkModelException {
    Set<Query> queries = new HashSet<>();
    for(ColumnAttributeField attribute : columnAttributes) {
      queries.add(attribute.getColumn().getQuery());
    }
    return queries;
  }
	
  /**
   * Collect all the queries needed to accommodate the requested tables.
   * @return a set of table queries
   * @throws WdkModelException
   */
  public Set<Query> getTableQueries() throws WdkModelException {
    Set<Query> queries = new HashSet<>();
    for(TableField table : _tables) {
  	  Query tableQuery = table.getQuery();
	  queries.add((Query) _answerValue.getQuestion().getWdkModel().resolveReference(tableQuery.getFullName()));
	}
    return queries;
  }
	
  /**
   * For a given query, collects all the requested column attributes served by the query, finds their corresponding
   * column names and returns that list of column names.
   * @param query - the query under consideration
   * @return an ordered list of those column names, served the the given query, that are associated with the attributes requested.
   * @throws WdkModelException
   */
  public List<String> getQueryColumns(Query query, List<ColumnAttributeField> columnAttributes) throws WdkModelException {
    List<String> columnNames = new ArrayList<>();
    for(ColumnAttributeField attribute : columnAttributes) {
  	  if(query == attribute.getColumn().getQuery()) {  
	    columnNames.add(attribute.getColumn().getName());
	  }  
	}
    return columnNames;
  }
	
  /**
   * Produces an ordered map of the queries required to satisfy the attributes requested to the ordered list of the
   * column names associated with attributes satisfied by each query
   * @param columnAttributes
   * @return
   * @throws WdkModelException
   * @throws WdkUserException
   */
  public Map<Query,List<String>> assembleAttributeQueryColumnMap(List<ColumnAttributeField> columnAttributes) throws WdkModelException, WdkUserException {
    Map<Query,List<String>> queryColumnNameMap = new LinkedHashMap<>();
    for(Query query : getAttributeQueries(columnAttributes)) {
  	queryColumnNameMap.put(query, getQueryColumns(query, columnAttributes));
    }
    return queryColumnNameMap;
  }
	
  /**
   * Temporary addition for development purpose to chmod all generated files with world write permissions so
   * they can be deleted from the temporary file directory manually.
   */
  public void makeFilesWorldWriteable() {
    for(Path filePath : _attributeFileMap.keySet()) {
      File file = new File(filePath.toString());
  	  file.setWritable(true, false);
    }
	for(Path filePath : _tableFileMap.keySet()) {
      File file = new File(filePath.toString());
      file.setWritable(true, false);
    }
  }
  
  /**
   * Assembles the CSV file at the given file path for given resultList, copying out, in order, all
   * the columns provided in the columnNames list.  The CSV file columns are tab delimited and any
   * nulls are provided with a unique representation so that they may be turned back into nulls when
   * later creating ad hoc record instances.
   * @param filePath - the path to the temporary file that will house the CSV data
   * @param columnNames - an ordered list of the names to be extracted from the query results
   * @param resultList - the query results
   * @throws WdkModelException
   */
  public void assembleCsvFile(Path filePath, List<String> columnNames, ResultList resultList) throws WdkModelException {
	  
    try(CSVWriter writer = new CSVWriter(new BufferedWriter(new FileWriter(filePath.toString()), BUFFER_SIZE), CsvResultList.TAB)) {
	 
  	  // Create an array sized to handle the attribute columns as this will be used to write out a row
  	  // to the CSV file.  Primary keys are not included since there should be exactly 1 row per primary key
  	  // and since restoration to record instances will employ the same sorted id SQL.
	  String[] inputs = new String[columnNames.size()];
		
	  // For each record in the result list
	  while(resultList.next()) {
        int i = 0;
        for (String columnName : columnNames) {
          Object result = resultList.get(columnName);
          inputs[i] = (result == null) ? CsvResultList.NULL_REPRESENTATION : String.valueOf(result); 
	      i++;
	    }
	    writer.writeNext(inputs);
  	  }
	} 
    catch (IOException ioe) {
	  throw new WdkModelException(ioe.getMessage());
	}
  }

  /**
   * Writes out one file per executed query to the temporary directory.  A may of attribute file paths
   * to their associated list of attribute fields is created to facilitate later population of a record instance.
   * @throws WdkModelException
   * @throws WdkUserException
   */
  public void assembleAttributeFiles() throws WdkModelException, WdkUserException {
    Timer t = new Timer();
    logger.info("Starting attribute file assembly...");
    Map<Query,List<String>> queryColumnNameMap = assembleAttributeQueryColumnMap(filterColumnAttributeFields(_attributes, false));
    logger.info("Assembled query column map : " + t.getElapsedString());
    SqlResultList resultList = null;
    DataSource dataSource = _answerValue.getQuestion().getWdkModel().getAppDb().getDataSource();
   
    // Iterate over all the queries needed to return all the requested attributes
    for(Query query : queryColumnNameMap.keySet()) {
    
      // Obtain path to CSV file that will hold the results of the current query.
      Path filePath = _temporaryDirectory.resolve(query.getName() + TEMP_FILE_EXT);

      try {
    	logger.info("Starting query " + query.getName());
    	  
    	// Getting the paged attribute SQL but in fact, getting a SQL statement requesting with all records.  
    	_answerValue.setPageIndex(0, -1);
    	String sql = _answerValue.getPagedAttributeSql(query);
    	  
    	// Get the result list for the current attribute query 
	    resultList = new SqlResultList(SqlUtils.executeQuery(dataSource, sql, query.getFullName()  +  "__attr-full"));
	    
	    // Collect together all the columns representing the column attribute fields to be included
	    List<String> columnNames = queryColumnNameMap.get(query);
	   
      	// Create a entry relating the file name to an ordered list of the attribute fields
    	List<ColumnAttributeField> attributeFields = new ArrayList<>();
    	for(String columnName : columnNames) {
    	  ColumnAttributeField attributeField = (ColumnAttributeField) _answerValue.getQuestion().getAttributeFieldMap().get(query.getColumnMap().get(columnName).getName());
          attributeFields.add(attributeField);
    	}
    	  
    	// Add the path to the temporary CSV file and an ordered list of the attribute fields
    	// to a map to be used when restoring the data to record instances.
    	_attributeFileMap.put(filePath, attributeFields);

	    // Transfer the result list content to the CSV file provided.
      	logger.info("Starting iteration over result list for query " + query.getName() + ": " + t.getElapsedString());
      	assembleCsvFile(filePath, columnNames, resultList);	
        logger.info("Finished iteration over result list for query " + query.getName() + ": " + t.getElapsedString());
      }
      catch(WdkModelException | SQLException e) {
        throw new WdkModelException(e.getMessage());  
      }
      finally {
    	
        // Close the result list for the table query if one exists.  	
        if(resultList != null) {  
          resultList.close();
        }  
      }
    }
    logger.info("Attribute file assembly complete : " + t.getElapsedString());
  }
	
  /**
   * Constructs one temporary CSV file for each table requested.  A map of table file
   * paths to their associated table field is created to facilitate later population of a record instance.
   * @throws WdkModelException
   * @throws WdkUserException
   */
  public void assembleTableFiles() throws WdkModelException, WdkUserException {
    Timer t = new Timer();
    logger.info("Starting table file assembly...");
    
    // Iterate over all the tables requested
    for(TableField table : _tables) {
      logger.info("Starting table: " + table.getName());
      
      // Appending table designation to query name for file to more easily distinguish 
      // these files from those supporting attribute queries and to avoid name collisions.
      Path filePath = _temporaryDirectory.resolve(table.getQuery().getName() + TABLE_DESIGNATION + TEMP_FILE_EXT);
      
      // Add the path to the temporary CSV file and the table field to a map to be used when
      // restoring the data to record instances.  Not cherry-picking columns here so we can
      // rely on the order in which column attribute fields are returned in the API.
      _tableFileMap.put(filePath, table);
      
      ResultList resultList = null;
      try {
    	
    	// Get the result list for the current table query
        resultList = _answerValue.getTableFieldResultList(table);
     
        // Collect together all the columns representing the primary key attribute fields.
      	PrimaryKeyAttributeField pkField = _answerValue.getQuestion().getRecordClass().getPrimaryKeyAttributeField();
      	String[] pkColumns = pkField.getColumnRefs();

        // Collect together all the columns representing the column attribute fields to be included
        List<String> attributeColumns = new ArrayList<>();
        List<ColumnAttributeField> fields = filterColumnAttributeFields(Arrays.asList(table.getAttributeFields()),true);
        for(ColumnAttributeField field : fields) {
    	  attributeColumns.add(field.getColumn().getName());
    	}
        
        // Combine the primary key columns and column attribute columns into a single list
        List<String> columnNames = new ArrayList<>(Arrays.asList(pkColumns));
        columnNames.addAll(attributeColumns);
  
        // Transfer the result list content to the CSV file provided.
        logger.info("Starting iteration over result list for query " + table.getQuery().getName() + ": " + t.getElapsedString());  
        assembleCsvFile(filePath, columnNames, resultList);	
        logger.info("Finished iteration over result list for query " + table.getQuery().getName() + ": " + t.getElapsedString());	
        
      }
   	  finally {
   		  
   		// Close the result list for the table query if one exists.  
   	    if(resultList != null) {
   	      resultList.close();
   	    }
      }
    }
    logger.info("Table file assembly complete : " + t.getElapsedString());
  }

  /**
   * Creates an iterator which can construct RecordInstance records on the fly
   * from the files produced by populateFiles().  Please note that the RecordInstances
   * returned by the iterator will ONLY have the fields and attributes specified in
   * this class's constructor; additional fields and attributes cannot be added at-will.
   */
  @Override
  public Iterator<RecordInstance> iterator() {
    if (!_filesPopulated) {
      throw new IllegalStateException("Iterators are not available from this class until files are populated.");
    }
    FileBasedRecordIterator iter;
	try {
	  iter = new FileBasedRecordIterator(_answerValue, _attributeFileMap, _tableFileMap);
	}
	catch (WdkModelException | WdkUserException e) {
	  throw new RuntimeException(e.getMessage());
	}
	_iterators.add(iter);
    return iter;
  }

  /**
   * Closes and removes all files written by this object and alerts any
   * returned iterators that files will no longer be available.  Files will
   * be closed quietly
   */
  @Override
  public void close() {
    _filesPopulated = false;
    for (FileBasedRecordIterator iter : _iterators) {
      iter.close();
    }
    
    //TODO  Uncomment for production
    // Removing temporary dir and resident files created to populate on the fly record instances.
//    try {
//      FileUtils.forceDelete(new File(_temporaryDirectory.toString()));
//    }
//    catch(IOException ioe) {
//      logger.warn("Unable to remove temporary directory");
//    }
  }
}
