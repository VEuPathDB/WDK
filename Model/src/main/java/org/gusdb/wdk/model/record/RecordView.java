package org.gusdb.wdk.model.record;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkView;

public class RecordView extends WdkView {

    public static RecordView[] createSupportedRecordViews() {
        List<RecordView> views = new ArrayList<RecordView>();
        views.add(createDefaultRecordView());
        return views.toArray(new RecordView[0]);
    }

    private static RecordView createDefaultRecordView() {
        RecordView view = new RecordView();
        view.setName("_default");
        view.setDisplay("Overview");
        return view;
    }

    private String handlerClass;
    private RecordViewHandler handler;

    public RecordViewHandler getHandler() {
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
                Class<? extends RecordViewHandler> hClass = Class.forName(
                        handlerClass).asSubclass(RecordViewHandler.class);
                handler = hClass.getDeclaredConstructor().newInstance();
            }
            catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                IllegalArgumentException | InvocationTargetException |
                NoSuchMethodException | SecurityException ex) {
              throw new WdkModelException(ex);
            }
        }
    }
}
