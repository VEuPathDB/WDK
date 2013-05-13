package org.gusdb.wdk.model.answer;

import java.util.ArrayList;
import java.util.List;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkView;
import org.gusdb.wdk.model.record.RecordClass;

public class SummaryView extends WdkView {

    public static SummaryView[] createSupportedSummaryViews(RecordClass recordClass) {
        List<SummaryView> views = new ArrayList<SummaryView>();
        views.add(createDefaultSummaryView(recordClass));
        return views.toArray(new SummaryView[0]);
    }

    private static SummaryView createDefaultSummaryView(RecordClass recordClass) {
        SummaryView view = new SummaryView();
        view.setName("_default");
        //view.setDisplay(recordClass.getDisplayName() + " Results");
	// basket shares the tab title with the results table, so the tab should say "Genes" not "Gene results"
	view.setDisplay(recordClass.getDisplayName() + " Results");
        view.setJsp("/wdk/jsp/results/default.jsp");
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
    public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
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
