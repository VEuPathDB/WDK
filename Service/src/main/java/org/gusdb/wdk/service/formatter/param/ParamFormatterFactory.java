package org.gusdb.wdk.service.formatter.param;

import org.gusdb.wdk.model.query.param.AbstractEnumParam;
import org.gusdb.wdk.model.query.param.AbstractEnumParam.DisplayType;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.query.param.DatasetParam;
import org.gusdb.wdk.model.query.param.DateParam;
import org.gusdb.wdk.model.query.param.DateRangeParam;
import org.gusdb.wdk.model.query.param.FilterParamNew;
import org.gusdb.wdk.model.query.param.NumberParam;
import org.gusdb.wdk.model.query.param.NumberRangeParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.param.StringParam;
import org.gusdb.wdk.model.query.param.TimestampParam;

public class ParamFormatterFactory {

  public static ParamFormatter<?> getFormatter(Param param) throws IllegalArgumentException {

    if (param instanceof FilterParamNew) {
      return new FilterParamNewFormatter((FilterParamNew)param);
    }
    if (param instanceof AbstractEnumParam) {
      AbstractEnumParam enumParam = (AbstractEnumParam)param;
      return (enumParam.getDisplayType().equals(DisplayType.TREEBOX.getValue()) ?
          new TreeBoxEnumParamFormatter(enumParam) :
          new EnumParamFormatter(enumParam));
    }
    if (param instanceof AnswerParam) {
      return new AnswerParamFormatter((AnswerParam)param);
    }
    if (param instanceof DatasetParam) {
      return new DatasetParamFormatter((DatasetParam)param);
    }
    if (param instanceof DateParam) {
      return new DateParamFormatter((DateParam)param);
    }
    if (param instanceof DateRangeParam) {
      return new DateRangeParamFormatter((DateRangeParam)param);
    }
    if (param instanceof NumberParam) {
      return new NumberParamFormatter((NumberParam)param);
    }
    if (param instanceof NumberRangeParam) {
      return new NumberRangeParamFormatter((NumberRangeParam)param);
    }
    if (param instanceof StringParam) {
      return new StringParamFormatter((StringParam)param);
    }
    if (param instanceof TimestampParam) {
      return new TimestampParamFormatter((TimestampParam)param);
    }

    throw new IllegalArgumentException("Param class '" + param.getClass().getName() + "' does not have a formatter.");
  }
}
