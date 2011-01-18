package org.gusdb.wdk.model.query.param;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.User;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author xingao
 * 
 *         raw value: an json string that identifies the remote resource
 * 
 *         dependent value: an compressed version of the raw value
 * 
 *         independent value: same as dependent value
 * 
 *         internal value: a sql that returns the locally cached data of the
 *         remote resource.
 * 
 */
public class RemoteListParam extends Param {

    public static final String PARAM_RAW_VALUE = "raw-value";

    private List<RemoteHandlerReference> listHandlerReferences = new ArrayList<RemoteHandlerReference>();
    private RemoteHandlerReference listHandlerReference;
    private RemoteHandler listHandler;

    private List<RemoteHandlerReference> internalHandlerReferences = new ArrayList<RemoteHandlerReference>();
    private RemoteHandlerReference internalHandlerReference;
    private RemoteHandler internalHandler;

    public RemoteListParam() {}

    /**
     * handler, and handlerReference are not cloned.
     * 
     * @param param
     */
    public RemoteListParam(RemoteListParam param) {
        super(param);

        this.listHandlerReference = param.listHandlerReference;
        this.listHandler = param.listHandler;
        if (listHandlerReferences != null)
            this.listHandlerReferences = new ArrayList<RemoteHandlerReference>(
                    param.listHandlerReferences);
    }

    @Override
    public Param clone() {
        return new RemoteListParam(this);
    }

    @Override
    public void excludeResources(String projectId) throws WdkModelException {
        super.excludeResources(projectId);

        // exclude resources of list handler reference
        for (RemoteHandlerReference reference : listHandlerReferences) {
            if (reference.include(projectId)) {
                reference.excludeResources(projectId);

                if (this.listHandlerReference != null)
                    throw new WdkModelException("remote handler for the list "
                            + "of remoteListParam " + getFullName()
                            + " is duplicated.");

                this.listHandlerReference = reference;
            }
        }
        this.listHandlerReferences = null;
        if (listHandlerReference == null)
            throw new WdkModelException("remote handler for the list is not "
                    + "defined in remoteListParam " + getFullName());

        // exclude resources of internal handler reference
        for (RemoteHandlerReference reference : internalHandlerReferences) {
            if (reference.include(projectId)) {
                reference.excludeResources(projectId);

                if (this.internalHandlerReference != null)
                    throw new WdkModelException("remote handler for the "
                            + "internal of remoteListParam " + getFullName()
                            + " is duplicated.");

                this.internalHandlerReference = reference;
            }
        }
        this.internalHandlerReferences = null;
        if (internalHandlerReference == null)
            throw new WdkModelException("Remote handler for the internal is "
                    + "not defined in remoteListParam " + getFullName());
    }

    @Override
    public void resolveReferences(WdkModel wdkModel) throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        if (resolved) return;

        super.resolveReferences(wdkModel);

        try {
            // resolve the list handler class
            String className = listHandlerReference.getHandlerClass();
            Class<RemoteHandler> handlerClass = (Class<RemoteHandler>) Class.forName(className);
            listHandler = handlerClass.newInstance();
            listHandler.setModel(wdkModel);
            Map<String, String> settings = listHandlerReference.getProperties();
            listHandler.setProperties(settings);

            // resolve the internal handler class
            className = internalHandlerReference.getHandlerClass();
            handlerClass = (Class<RemoteHandler>) Class.forName(className);
            internalHandler = handlerClass.newInstance();
            internalHandler.setModel(wdkModel);
            settings = internalHandlerReference.getProperties();
            internalHandler.setProperties(settings);
        } catch (ClassNotFoundException ex) {
            throw new WdkModelException(ex);
        } catch (InstantiationException ex) {
            throw new WdkModelException(ex);
        } catch (IllegalAccessException ex) {
            throw new WdkModelException(ex);
        }
    }

    @Override
    protected void appendJSONContent(JSONObject jsParam, boolean extra)
            throws JSONException {
        jsParam.put("list-handler", listHandler.getClass().getName());
        jsParam.put("internal-handler", internalHandler.getClass().getName());
    }

    @Override
    protected void applySuggection(ParamSuggestion suggest) {
    // do nothing
    }

    @Override
    public String dependentValueToIndependentValue(User user,
            String dependentValue) throws NoSuchAlgorithmException,
            WdkUserException, WdkModelException, SQLException, JSONException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String dependentValueToInternalValue(User user, String dependentValue)
            throws WdkModelException, NoSuchAlgorithmException, SQLException,
            JSONException, WdkUserException {
        String rawValue = decompressValue(dependentValue);
        if (rawValue == null || rawValue.length() == 0) rawValue = emptyValue;
        if (isNoTranslation()) return rawValue;

        Map<String, String> params = new LinkedHashMap<String, String>();
        params.put(PARAM_RAW_VALUE, rawValue);
        return internalHandler.getResource(user, params);
    }

    @Override
    public String dependentValueToRawValue(User user, String dependentValue)
            throws WdkModelException, NoSuchAlgorithmException,
            WdkUserException, SQLException, JSONException {
        return decompressValue(dependentValue);
    }

    @Override
    public String rawOrDependentValueToDependentValue(User user, String rawValue)
            throws NoSuchAlgorithmException, WdkModelException,
            WdkUserException, SQLException, JSONException {
        return compressValue(rawValue);
    }

    @Override
    protected void validateValue(User user, String rawOrDependentValue)
            throws WdkModelException, NoSuchAlgorithmException, SQLException,
            JSONException, WdkUserException {
    // do nothing
    }

    public Map<String, String> getList(User user) throws JSONException,
            WdkModelException {
        Map<String, String> params = new LinkedHashMap<String, String>();
        String resource = listHandler.getResource(user, params);
        JSONArray jsResource = new JSONArray(resource);
        Map<String, String> list = new LinkedHashMap<String, String>();
        for (int i = 0; i < jsResource.length(); i++) {
            JSONObject tuple = jsResource.getJSONObject(i);
            String term = tuple.getString("term");
            String display = tuple.getString("display");
            list.put(term, display);
        }
        return list;
    }
    
    public void addListHandler(RemoteHandlerReference reference) {
        this.listHandlerReferences.add(reference);
    }
    
    public void addInternalHandler(RemoteHandlerReference reference) {
        this.internalHandlerReferences.add(reference);
    }
}
