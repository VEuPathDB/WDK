package org.gusdb.wdk.model.toolbundle.reporter.report;

import io.vulpine.lib.json.schema.Schema;
import io.vulpine.lib.json.schema.SchemaBuilder;
import org.gusdb.fgputil.SortDirection;
import org.gusdb.fgputil.runtime.JvmUtil;

public class StringReport extends AbstractReport<String> {

  private final static byte SIZE_PREFIX = (byte) (
    JvmUtil.OBJECT_HEADER_SIZE * 2 // String, byte[]
      + Integer.BYTES * 2          // byte[].length, String.hash
      + Byte.BYTES                 // String.coder
  );

  public StringReport(long maxVals, SortDirection sort) {
    super(maxVals, sort);
  }

  public StringReport(SortDirection sort) {
    super(sort);
  }

  @Override
  public String parse(String raw) {
    return raw;
  }

  @Override
  protected int sizeOf(String val) {
    return padSize(SIZE_PREFIX + val.getBytes().length);
  }

  public static SchemaBuilder outputSchema() {
    var js = Schema.draft4();
    return AbstractReport.outputSpec()
      .optionalProperty(KEY_VALUES, js.asArray()
        .items(js.asObject()
          .requiredProperty(Pair.KEY_VALUE, js.asString())
          .requiredProperty(Pair.KEY_COUNT, js.asInteger().minimum(0)))
        .description("An array distinct values and their frequency")
        .uniqueItems(true));
  }
}
