package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.param.AbstractEnumParam;
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

public class ParamBeanFactory {

  @SuppressWarnings("unchecked")
  public static <T extends Param> ParamBean<T> createBeanFromParam(
      WdkModel wdkModel, UserBean user, T param) throws WdkModelException {
    ParamBean<T> bean;
    if (user == null) {
      // FIXME - need to get the actual user in the future.
      user = new UserBean(wdkModel.getSystemUser());
    }
    if (param instanceof FilterParamNew) {
      bean = (ParamBean<T>) new FilterParamNewBean((FilterParamNew)param);
    }
    else if (param instanceof AbstractEnumParam) {
      bean = (ParamBean<T>) new EnumParamBean((AbstractEnumParam)param);
    }
    else if (param instanceof AnswerParam) {
      bean = (ParamBean<T>) new AnswerParamBean((AnswerParam)param);
    }
    else if (param instanceof DatasetParam) {
      bean = (ParamBean<T>) new DatasetParamBean((DatasetParam)param);
    }
    else if (param instanceof TimestampParam) {
      bean = (ParamBean<T>) new TimestampParamBean((TimestampParam)param);
    }
    else if (param instanceof StringParam) {
      bean = (ParamBean<T>) new StringParamBean((StringParam)param);
    }
    else if (param instanceof NumberParam) {
      bean = (ParamBean<T>) new NumberParamBean((NumberParam)param);
    }
    else if (param instanceof DateParam) {
      bean = (ParamBean<T>) new DateParamBean((DateParam)param);
    }
    else if (param instanceof NumberRangeParam) {
      bean = (ParamBean<T>) new NumberRangeParamBean((NumberRangeParam)param);
    }
    else if (param instanceof DateRangeParam) {
      bean = (ParamBean<T>) new DateRangeParamBean((DateRangeParam)param);
    }
    else {
      throw new WdkModelException("Unknown param type: " + param.getClass().getCanonicalName());
    }
    bean.setUser(user);
    return bean;
  }
}
