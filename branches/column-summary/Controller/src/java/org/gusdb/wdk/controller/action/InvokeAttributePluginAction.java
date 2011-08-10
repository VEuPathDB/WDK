package org.gusdb.wdk.controller.action;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.model.attribute.plugin.AttributePlugin;
import org.gusdb.wdk.model.jspwrap.AnswerValueBean;
import org.gusdb.wdk.model.jspwrap.AttributeFieldBean;
import org.gusdb.wdk.model.jspwrap.RecordClassBean;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.UserBean;

public class InvokeAttributePluginAction extends Action {
    
    private static final String PARAM_STEP = "step";
    private static final String PARAM_ATTRIBUTE = "attribute";
    private static final String PARAM_PLUGIN = "plugin";
    
    private Logger logger = Logger.getLogger(InvokeAttributePluginAction.class);
    
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        logger.debug("Entering InvokeAttributePluginAction...");
        UserBean user = ActionUtility.getUser(servlet, request);
        
        // get the step
        String strStep = request.getParameter(PARAM_STEP);
        int stepId = Integer.valueOf(strStep);
        StepBean step = user.getStep(stepId);
        
        // get the attribute
        String attributeName = request.getParameter(PARAM_ATTRIBUTE);
        RecordClassBean recordClass = step.getQuestion().getRecordClass();
        AttributeFieldBean attribute = recordClass.getAttributeFields().get(attributeName);
        
        // get the plugin
        String pluginName = request.getParameter(PARAM_PLUGIN);
        AttributePlugin plugin = attribute.getAttributePlugins().get(pluginName);
        
        AnswerValueBean answerValue = step.getAnswerValue();
        logger.debug("Processing attribute plugin: " + pluginName);
        Map<String, Object> results = plugin.process(answerValue.getAnswerValue());
        for (String key : results.keySet()) {
            request.setAttribute(key, results.get(key));
        }
        
        // get the view
        String view = plugin.getView();
        
        logger.debug("Leaving InvokeAttributePluginAction. view: " + view);
        return new ActionForward(view, false);
    }
}
