package org.gusdb.wdk.model.bundle.reporter;

import com.fasterxml.jackson.databind.JsonNode;
import io.vulpine.lib.json.schema.Schema;
import io.vulpine.lib.json.schema.SchemaBuilder;
import org.gusdb.fgputil.functional.Result;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.bundle.ColumnReporter;
import org.gusdb.wdk.model.bundle.impl.AbstractColumnTool;
import org.gusdb.wdk.model.record.attribute.AttributeFieldDataType;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.Stack;
import java.util.stream.Collectors;

import static org.gusdb.fgputil.FormatUtil.NL;

public class MultiTypeColumnReporter extends AbstractColumnReporter {

  private ColumnReporter[] reporters = {
    new NumberColumnReporter(),
    new StringColumnReporter(),
    new DateColumnReporter()
  };

  private Result<WdkModelException, ColumnReporter> handler;

  @Override
  public SchemaBuilder inputSpec() {
    var js = Schema.draft4().oneOf();

    for (var rep : reporters)
      if (rep.isCompatibleWith(getColumn().getDataType()))
        js.add(rep.inputSpec());

    return js;
  }

  @Override
  public SchemaBuilder outputSpec() {
    var js = Arrays.stream(reporters)
      .filter(r -> r.isCompatibleWith(getColumn().getDataType()))
      .map(ColumnReporter::outputSpec)
      .toArray(SchemaBuilder[]::new);

    if (js.length == 1)
      return js[0];


    var out = Schema.draft4().oneOf();
    for (var rep : js)
      out.add(rep);

    return out;
  }

  @Override
  public ColumnReporter copy() {
    final var out = copyInto(new MultiTypeColumnReporter());

    out.reporters = new ColumnReporter[reporters.length];
    for (var i = 0; i < reporters.length; i++)
      out.reporters[i] = reporters[i].copy();

    return out;
  }

  @Override
  public boolean isCompatibleWith(AttributeFieldDataType type) {
    for (var rep : reporters)
      if (rep.isCompatibleWith(type))
        return true;
    return false;
  }

  @Override
  public boolean isCompatibleWith(JsonNode js) {
    for (var rep : reporters)
      if (rep.isCompatibleWith(js))
        return true;
    return false;
  }

  @Override
  public Aggregator build(OutputStream out) throws WdkModelException {
    return handler.valueOrElseThrow().build(out);
  }

  @Override
  public void parseConfig(JsonNode config) {
    handler = pickHandler();
    if (handler.isValue())
      if (handler.getValue() instanceof AbstractColumnTool)
        ((AbstractColumnTool) handler.getValue()).parseConfig(config);
  }

  private Result<WdkModelException, ColumnReporter> pickHandler() {
    var ress = Arrays.stream(reporters)
      .filter(r -> r.isCompatibleWith(getColumn().getDataType()))
      .filter(r -> r.isCompatibleWith(getConfig()))
      .map(this::checkConf)
      .map(r -> r.mapError(WdkModelException::new))
      .collect(Collectors.toList());

    var errs = new StringBuilder();
    var pick = new Stack<ColumnReporter>();
    for (var res : ress)
      if (res.isError())
        errs.append(res.getError().getMessage()).append(NL);
      else
        pick.push(res.getValue());

    if (pick.size() == 1)
      return Result.value(pick.pop());

    if (pick.size() > 1)
      return Result.error(new WdkModelException(
        "More than one reporter matches the given configuration on this column."
          + "  Matching reporters: "
          + pick.stream()
            .map(Object::getClass)
            .map(Class::getSimpleName)
            .collect(Collectors.joining(", "))
          + NL
          + errs.toString()));

    return Result.error(new WdkModelException(errs.toString()));
  }

  private Result<WdkUserException, ColumnReporter>
  checkConf(ColumnReporter reporter) {
    return Result.of(() -> {
      reporter.validateConfig(getConfig());
      return reporter;
    });
  }
}
