package org.gusdb.wdk.service.formatter.param;

import org.gusdb.wdk.model.query.param.AbstractEnumParam;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.query.param.DatasetParam;
import org.gusdb.wdk.model.query.param.FilterParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.param.StringParam;

public class ParamFormatterFactory {

  public static ParamFormatter<?> getFormatter(Param param) throws IllegalArgumentException {

    if (param instanceof FilterParam) {
      return new FilterParamFormatter((FilterParam)param);
    }
    if (param instanceof AbstractEnumParam) {
      AbstractEnumParam enumParam = (AbstractEnumParam)param;
      return (enumParam.getDisplayType().equals("typeAhead") ?
          new TypeAheadParamFormatter(enumParam) :
          new EnumParamFormatter(enumParam));
    }
    if (param instanceof AnswerParam) {
      return new AnswerParamFormatter((AnswerParam)param);
    }
    if (param instanceof DatasetParam) {
      return new DatasetParamFormatter((DatasetParam)param);
    }
    if (param instanceof StringParam) {
      return new StringParamFormatter((StringParam)param);
    }

    // basic formatter for TimestampParam and any other "simple" params
    return new ParamFormatter<Param>(param);
  }
}
