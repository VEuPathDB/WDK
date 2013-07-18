package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.param.AbstractEnumParam;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.query.param.DatasetParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.param.StringParam;
import org.gusdb.wdk.model.query.param.TimestampParam;

public class ParamBeanFactory {

    @SuppressWarnings("unchecked")
	public static <T extends Param> ParamBean<T> createBeanFromParam(UserBean user, T param) throws WdkModelException {
    	ParamBean<T> bean;
        if (param instanceof AbstractEnumParam) {
            bean = (ParamBean<T>) new EnumParamBean((AbstractEnumParam)param);
        } else if (param instanceof AnswerParam) {
            bean = (ParamBean<T>) new AnswerParamBean((AnswerParam)param);
        } else if (param instanceof DatasetParam) {
            bean = (ParamBean<T>) new DatasetParamBean((DatasetParam)param);
        } else if (param instanceof TimestampParam) {
            bean = (ParamBean<T>) new TimestampParamBean((TimestampParam)param);
        } else if (param instanceof StringParam) {
            bean = (ParamBean<T>) new StringParamBean((StringParam)param);
        } else {
            throw new WdkModelException("Unknown param type: " + param.getClass().getCanonicalName());
        }
        bean.setUser(user);
        return bean;
    }
}
