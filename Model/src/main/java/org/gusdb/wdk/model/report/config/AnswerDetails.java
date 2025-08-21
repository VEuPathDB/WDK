package org.gusdb.wdk.model.report.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.gusdb.fgputil.SortDirectionSpec;
import org.gusdb.wdk.model.record.TableField;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.report.Reporter.ContentDisposition;

public class AnswerDetails {

  public static final Integer ALL_RECORDS = -1;

  public enum AttributeFormat {
    TEXT, DISPLAY, EXPANDED;
  }

  // use factory method to construct from JSON
  AnswerDetails() {}

  // all fields are private
  private int _offset = 0; // default start at first record
  private int _numRecords = ALL_RECORDS; // default to all records
  private Map<String, AttributeField> _attributes = Collections.emptyMap();
  private Map<String, TableField> _tables = Collections.emptyMap();
  private List<SortDirectionSpec<AttributeField>> _sorting = new ArrayList<>();
  private ContentDisposition _contentDisposition = ContentDisposition.INLINE;
  private AttributeFormat _attributeFormat = AttributeFormat.DISPLAY;
  private boolean _isBufferEntireResponse = false; // default to standard streaming model

  // all getters are public
  public int getOffset() { return _offset; }
  public int getNumRecords() {  return _numRecords; }
  public Map<String, AttributeField> getAttributes() { return _attributes; }
  public Map<String, TableField> getTables() { return _tables; }
  public List<SortDirectionSpec<AttributeField>> getSorting() { return _sorting; }
  public ContentDisposition getContentDisposition() { return _contentDisposition; }
  public AttributeFormat getAttributeFormat() { return _attributeFormat; }
  public boolean isBufferEntireResponse() { return _isBufferEntireResponse; }

  // all setters are package-private, available only to factory
  void setOffset(int offset) { _offset = offset; }
  void setNumRecords(int numRecords) { _numRecords = numRecords; }
  void setAttributes(Map<String, AttributeField> attributes) { _attributes = attributes; }
  void setTables(Map<String, TableField> tables) { _tables = tables; }
  void setSorting(List<SortDirectionSpec<AttributeField>> sorting) { _sorting = sorting; }
  void setContentDisposition(ContentDisposition contentDisposition) { _contentDisposition = contentDisposition; }
  void setAttributeFormat(AttributeFormat attributeFormat) { _attributeFormat = attributeFormat; }
  public void setBufferEntireResponse(boolean isBufferEntireResponse) { _isBufferEntireResponse = isBufferEntireResponse; }

}
