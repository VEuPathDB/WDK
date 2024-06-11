package org.gusdb.wdk.model.record;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import org.gusdb.fgputil.db.stream.ResultSetIterator;

public class ResultSetPrimaryKeyIterator extends ResultSetIterator<String[]> implements PrimaryKeyIterator {

  private static class PrimaryKeyRowConverter implements RowConverter<String[]> {

    private final String[] _pkColumns;

    public PrimaryKeyRowConverter(PrimaryKeyDefinition pkDef) {
      _pkColumns = pkDef.getColumnRefs();
    }

    @Override
    public Optional<String[]> convert(ResultSet rs) throws SQLException {
      String[] values = new String[_pkColumns.length];
      for (int i = 0; i < _pkColumns.length; i++) {
        Object value = rs.getObject(_pkColumns[i]);
        values[i] = (value == null) ? null : value.toString();
      }
      return Optional.of(values);
    }
  }

  public ResultSetPrimaryKeyIterator(PrimaryKeyDefinition pkDef, ResultSet rs) {
    super(rs, new PrimaryKeyRowConverter(pkDef));
  }

}
