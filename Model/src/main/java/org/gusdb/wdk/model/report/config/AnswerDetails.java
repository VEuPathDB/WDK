package org.gusdb.wdk.model.report.config;

import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.record.TableField;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.report.Reporter.ContentDisposition;
import org.gusdb.wdk.service.request.answer.AttributeFieldSortSpec;

public class AnswerDetails {

  static final Integer ALL_RECORDS = -1;

  // use factory method to construct from JSON
  AnswerDetails() {}

  // all fields are private
  private int _offset = 0; // default start at first record
  private int _numRecords = ALL_RECORDS; // default to all records
  private Map<String, AttributeField> _attributes;
  private Map<String, TableField> _tables;
  private List<AttributeFieldSortSpec> _sorting;
  private ContentDisposition _contentDisposition = ContentDisposition.INLINE;

  // all getters are public
  public int getOffset() { return _offset; }
  public int getNumRecords() {  return _numRecords; }
  public Map<String, AttributeField> getAttributes() { return _attributes; }
  public Map<String, TableField> getTables() { return _tables; }
  public List<AttributeFieldSortSpec> getSorting() { return _sorting; }
  public ContentDisposition getContentDisposition() { return _contentDisposition; }

  // all setters are package-private, available only to factory
  void setOffset(int offset) { _offset = offset; }
  void setNumRecords(int numRecords) { _numRecords = numRecords; }
  void setAttributes(Map<String, AttributeField> attributes) { _attributes = attributes; }
  void setTables(Map<String, TableField> tables) { _tables = tables; }
  void setSorting(List<AttributeFieldSortSpec> sorting) { _sorting = sorting; }
  void setContentDisposition(ContentDisposition contentDisposition) { _contentDisposition = contentDisposition; }

}
