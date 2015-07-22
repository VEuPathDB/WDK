package org.gusdb.wdk.model.answer;

import java.util.ArrayList;
import java.util.List;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkView;
import org.gusdb.wdk.model.record.RecordClass;

public class SummaryView extends WdkView {

  private static class DefaultSummaryView extends SummaryView {

    private final RecordClass recordClass;

    public DefaultSummaryView(RecordClass recordClass) {
      this.recordClass = recordClass;
      this.setName("_default");
      this.setJsp("/wdk/jsp/results/default.jsp");
    }

    @Override
    public String getDisplay() {
      return recordClass.getDisplayName() + " Results";
    }

  }

  public static SummaryView[] createSupportedSummaryViews(
      RecordClass recordClass) {
    List<SummaryView> views = new ArrayList<SummaryView>();
    views.add(createDefaultSummaryView(recordClass));
    return views.toArray(new SummaryView[1]);
  }

  private static SummaryView createDefaultSummaryView(RecordClass recordClass) {
    SummaryView view = new DefaultSummaryView(recordClass);
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

    if (handlerClass != null) { // resolve the handler class
      try {
        Class<? extends SummaryViewHandler> hClass = Class
            .forName(handlerClass).asSubclass(SummaryViewHandler.class);
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
