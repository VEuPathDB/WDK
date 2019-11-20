package org.gusdb.wdk.model.toolbundle.reporter;

import static org.gusdb.fgputil.FormatUtil.NL;

import java.util.Arrays;
import java.util.Stack;
import java.util.stream.Collectors;

import org.gusdb.fgputil.functional.Result;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.AttributeFieldDataType;
import org.gusdb.wdk.model.toolbundle.ColumnReporter;
import org.gusdb.wdk.model.toolbundle.ColumnReporterInstance;
import org.gusdb.wdk.model.toolbundle.ColumnToolConfig;
import org.gusdb.wdk.model.toolbundle.impl.AbstractColumnTool;

import com.fasterxml.jackson.databind.JsonNode;

import io.vulpine.lib.json.schema.Schema;
import io.vulpine.lib.json.schema.SchemaBuilder;

public class MultiTypeColumnReporter extends AbstractColumnTool<ColumnReporterInstance> implements ColumnReporter {

  private static final ColumnReporter[] REPORTERS = {
    new NumberColumnReporter(),
    new StringColumnReporter(),
    new DateColumnReporter()
  };

  @Override
  public SchemaBuilder getInputSpec(AttributeFieldDataType type) {
    var js = Schema.draft4().oneOf();
    for (var rep : REPORTERS)
      if (rep.isCompatibleWith(type))
        js.add(rep.getInputSpec(type));
    return js;
  }

  @Override
  public SchemaBuilder outputSpec(AttributeFieldDataType type) {
    var js = Arrays.stream(REPORTERS)
      .filter(r -> r.isCompatibleWith(type))
      .map(rep -> rep.outputSpec(type))
      .toArray(SchemaBuilder[]::new);

    if (js.length == 1)
      return js[0];

    var out = Schema.draft4().oneOf();
    for (var rep : js)
      out.add(rep);

    return out;
  }

  @Override
  public boolean isCompatibleWith(AttributeFieldDataType type) {
    for (var rep : REPORTERS)
      if (rep.isCompatibleWith(type))
        return true;
    return false;
  }

  @Override
  public boolean isCompatibleWith(AttributeFieldDataType type, JsonNode js) {
    for (var rep : REPORTERS)
      if (rep.isCompatibleWith(type, js))
        return true;
    return false;
  }

  @Override
  public ColumnReporterInstance makeInstance(AnswerValue answerValue, AttributeField field,
      ColumnToolConfig config) throws WdkModelException {
    return pickHandler(field.getDataType(), config.getConfig()).valueOrElseThrow()
        .makeInstance(answerValue, field, config);
  }

  private Result<WdkModelException, ColumnReporter> pickHandler(
      AttributeFieldDataType dataType, JsonNode jsonNode) {
    var ress = Arrays.stream(REPORTERS)
      .filter(r -> r.isCompatibleWith(dataType))
      .filter(r -> r.isCompatibleWith(dataType, jsonNode))
      .map(r -> Result.of(() -> { r.validateConfig(dataType, jsonNode); return r; }))
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
}
