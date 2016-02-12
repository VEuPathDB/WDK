package org.gusdb.wdk.model.answer;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkView;
import org.gusdb.wdk.model.record.RecordClass;

// a SummaryView is generated from the information on the WDK Model (xml)
public class SummaryView extends WdkView {

    public static SummaryView[] createSupportedSummaryViews(RecordClass recordClass)
            throws WdkModelException {
        SummaryView defaultSummaryView = new SummaryView(recordClass);
        return new SummaryView[]{ defaultSummaryView };
    }

    private String handlerClass;
    private SummaryViewHandler handler;

    // must create public no-arg constructor for Digester
    public SummaryView() { }

    /**
     * Creates a default summary view for the passed RecordClass.  This view
     * displays results in a WDK results table.
     * 
     * @param recordClass
     * @throws WdkModelException 
     */
    private SummaryView(RecordClass recordClass) throws WdkModelException {
        setName("_default");
        // FIXME: basket shares the tab title with the results table,
        //   so the tab should say "Genes" not "Gene results"
        setDisplay(recordClass.getDisplayName() + " Results");
        setJsp("/wdk/jsp/results/default.jsp");
        // NOTE: will leave handler class null; default handler class will be used
    }

    public SummaryViewHandler getHandler() {
        return handler;
    }

    public void setHandlerClass(String handlerClass) {
        this.handlerClass = handlerClass;
    }

    @Override
    public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
        super.resolveReferences(wdkModel);
        resolveHandlerClass();
    }
    
    private void resolveHandlerClass() throws WdkModelException {
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
