package org.gusdb.wdk.model.view;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.json.JSONException;

public class RecordView extends WdkView {

    public static RecordView[] createSupportedRecordViews() {
        List<RecordView> views = new ArrayList<RecordView>();
        views.add(createDefaultRecordView());
        return views.toArray(new RecordView[0]);
    }

    private static RecordView createDefaultRecordView() {
        RecordView view = new RecordView();
        view.setName("_default");
        view.setDisplay("Default");
        view.setDefault(true);
        view.setJsp("/wdk/jsp/records/default.jsp");
        return view;
    }

    private String handlerClass;
    private RecordViewHandler handler;

    public RecordViewHandler getHandler() {
        return handler;
    }

    public void setHandler(String handlerClass) {
        this.handlerClass = handlerClass;
    }

    @Override
    public void resolveReferences(WdkModel wdkModel) throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        super.resolveReferences(wdkModel);

        if (handlerClass != null) { // resolve the handler class
            try {
                Class<? extends RecordViewHandler> hClass = Class.forName(
                        handlerClass).asSubclass(RecordViewHandler.class);
                handler = hClass.newInstance();
            } catch (ClassNotFoundException ex) {
                throw new WdkModelException(ex);
            } catch (InstantiationException ex) {
                throw new WdkModelException(ex);
            } catch (IllegalAccessException ex) {
                throw new WdkModelException(ex);
            }
        }
    }
}
