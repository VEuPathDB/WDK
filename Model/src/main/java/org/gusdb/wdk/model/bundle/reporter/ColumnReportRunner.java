package org.gusdb.wdk.model.bundle.reporter;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.answer.stream.RecordStreamFactory;
import org.gusdb.wdk.model.bundle.ColumnReporter.Aggregator;
import org.gusdb.wdk.model.bundle.ColumnReporter.ReportRunner;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.record.attribute.AttributeField;

import java.util.Collections;

// TODO: Bad abstraction, unclear path to accomplish goal, replace this with
//       something that fits better into the call chain.
public class ColumnReportRunner implements ReportRunner {
  private final AnswerValue value;
  private final AttributeField column;

  public ColumnReportRunner(AnswerValue value, AttributeField column) {
    this.value  = value.cloneWithNewPaging(1, -1);
    this.column = column;
  }

  @Override
  public void run(Aggregator out) throws WdkModelException {
    try (
      var stream = RecordStreamFactory.getRecordStream(value,
        Collections.singletonList(column), Collections.emptyList())
    ) {
      for (var rec : stream) {
        var b = toAttrVal(rec);
        var c = out.parse(b);
        out.write(c);
      }

      out.close();
    }
  }

  private String toAttrVal(RecordInstance ri) throws WdkModelException {
    try {
      return ri.getAttributeValue(column.getName()).getValue();

    } catch (WdkUserException e) {
      throw new WdkModelException(e);
    }
  }
}
