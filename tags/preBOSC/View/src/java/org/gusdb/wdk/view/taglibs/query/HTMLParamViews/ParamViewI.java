package org.gusdb.gus.wdk.view.taglibs.query.HTMLParamViews;

import org.gusdb.gus.wdk.model.Param;

import java.io.IOException;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

/**
 * Interface indicating a class that can represent a Param in some view context
 */
public interface ParamViewI {

    void showParam(Param p, String formQuery, JspWriter out, PageContext pc) throws IOException;

}
