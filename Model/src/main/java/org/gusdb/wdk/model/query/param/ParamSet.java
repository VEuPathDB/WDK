package org.gusdb.wdk.model.query.param;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.ModelSetI;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;

/**
 * The paramSet is used to organize params into logical groups.
 * 
 * @author jerric
 *
 */
public class ParamSet extends WdkModelBase implements ModelSetI<Param> {

    private List<Param> _paramList = new ArrayList<Param>();
    private Map<String, Param> _paramMap = new LinkedHashMap<String, Param>();
    private String _name;

    private List<ParamConfiguration> _useTermOnlies = new ArrayList<ParamConfiguration>();
    private boolean _useTermOnly = false;

    public void setName(String name) {
        _name = name;
    }

    @Override
    public String getName() {
        return _name;
    }

    public Param getParam(String name) throws WdkModelException {
        Param q = _paramMap.get(name);
        if (q == null)
            throw new WdkModelException("Param Set " + getName()
                    + " does not include param " + name);
        return q;
    }

    @Override
    public Param getElement(String name) {
        return _paramMap.get(name);
    }

    public Param[] getParams() {
        Param[] array = new Param[_paramMap.size()];
        _paramMap.values().toArray(array);
        return array;
    }

    public boolean contains(String paramName) {
        return _paramMap.containsKey(paramName);
    }

    public void addParam(Param param) {
        param.setParamSet(this);
        if (_paramList != null) _paramList.add(param);
        else _paramMap.put(param.getName(), param);
    }

    public void addUseTermOnly(ParamConfiguration paramConfig) {
        _useTermOnlies.add(paramConfig);
    }

    public boolean isUseTermOnly() {
        return _useTermOnly;
    }

    @Override
    public void resolveReferences(WdkModel model) throws WdkModelException {
        for (Param param : _paramMap.values()) {
            param.resolveReferences(model);
        }
    }

    @Override
    public void setResources(WdkModel model) throws WdkModelException {
        for (Param param : _paramMap.values()) {
            param.setResources(model);
        }
    }

    @Override
    public String toString() {
        String newline = System.getProperty("line.separator");
        StringBuffer buf = new StringBuffer("ParamSet: name='" + _name + "'");
        buf.append(newline);

        for (Param param : _paramMap.values()) {
            buf.append(newline);
            buf.append(":::::::::::::::::::::::::::::::::::::::::::::");
            buf.append(newline);
            buf.append(param).append(newline);
        }
        return buf.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) throws WdkModelException {
        // exclude use term only. this must happen before processing params,
        // since enum/vocab params will use the value as default
        boolean hasUseTermOnly = false;
        for (ParamConfiguration paramConfig : _useTermOnlies) {
            if (paramConfig.include(projectId)) {
                if (hasUseTermOnly) {
                    throw new WdkModelException("paramSet " + getName()
                            + " has more than one <useTermOnly> tag "
                            + "for project " + projectId);
                } else {
                    _useTermOnly = paramConfig.isValue();
                    hasUseTermOnly = true;
                }
            }
        }
        _useTermOnlies = null;

        // exclude resources in each question
        for (Param param : _paramList) {
            // set the paramSet to each child param. The paramSet contains the
            // default value for the param, therefore it should happen before
            // excluding the resource from param
            if (param.include(projectId)) {
                param.excludeResources(projectId);
                String paramName = param.getName();

                if (_paramMap.containsKey(paramName))
                    throw new WdkModelException("Param named " + paramName
                            + " already exists in param set " + this._name);
                _paramMap.put(param.getName(), param);
            }
        }
        _paramList = null;
    }

    // ///////////////////////////////////////////////////////////////
    // ///// protected
    // ///////////////////////////////////////////////////////////////

}
