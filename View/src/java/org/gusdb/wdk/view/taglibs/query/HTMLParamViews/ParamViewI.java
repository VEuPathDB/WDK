package org.gusdb.gus.wdk.view.taglibs.query.HTMLParamViews;

import org.gusdb.gus.wdk.model.Param;

import java.io.IOException;

import javax.servlet.jsp.JspWriter;

public interface ParamViewI {

    void showParam(Param p, String formQuery, JspWriter out) throws IOException;

}
