package org.gusdb.wdk.model.view;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.json.JSONException;

public class SummaryView extends WdkView {

    public static SummaryView[] createSupportedSummaryViews() {
        List<SummaryView> views = new ArrayList<SummaryView>();
        views.add(createDefaultSummaryView());
        return views.toArray(new SummaryView[0]);
    }

    private static SummaryView createDefaultSummaryView() {
        SummaryView view = new SummaryView();
        view.setName("_default");
        view.setDisplay("Default");
        view.setDefault(true);
        view.setJsp("/wdk/jsp/results/table.jsp");
        return view;
    }

    private String handlerClass;
    private SummaryViewHandler handler;

    public SummaryViewHandler getHandler() {
        return handler;
    }

    public void setHandlerClass(String handlerClass) {
        this.handlerClass = handlerClass;
    }

    @Override
    public void resolveReferences(WdkModel wdkModel) throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        super.resolveReferences(wdkModel);

        if (handlerClass != null) {  // resolve the handler class
            try {
                Class<? extends SummaryViewHandler> hClass = Class.forName(
                        handlerClass).asSubclass(SummaryViewHandler.class);
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
