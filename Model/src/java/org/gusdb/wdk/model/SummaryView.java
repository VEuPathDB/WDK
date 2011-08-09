package org.gusdb.wdk.model;

import java.util.ArrayList;
import java.util.List;

public class SummaryView extends WdkModelBase {

    private String name;
    private String display;
    private String jsp;
    private boolean _default;

    public static SummaryView[] createSupportedSummaryViews() {
        List<SummaryView> views = new ArrayList<SummaryView>();
        views.add(createDefaultSummaryView());

        SummaryView[] array = new SummaryView[views.size()];
        views.toArray(array);
        return array;
    }

    private static SummaryView createDefaultSummaryView() {
        SummaryView view = new SummaryView();
        view.setName("_default");
        view.setDisplay("Default");
        view.setDefault(true);
        view.setJsp("/wdk/jsp/results/table.jsp");
        return view;
    }

    public static SummaryView[] createSupportedRecordViews() {
        List<SummaryView> views = new ArrayList<SummaryView>();
        views.add(createDefaultRecordView());

        SummaryView[] array = new SummaryView[views.size()];
        views.toArray(array);
        return array;
    }

    private static SummaryView createDefaultRecordView() {
        SummaryView view = new SummaryView();
        view.setName("_default");
        view.setDisplay("Default");
        view.setDefault(true);
        view.setJsp("/wdk/jsp/records/default.jsp");
        return view;
    }


    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the display
     */
    public String getDisplay() {
        return (display == null) ? name : display;
    }

    /**
     * @param display
     *            the display to set
     */
    public void setDisplay(String display) {
        this.display = display;
    }

    /**
     * @return the jsp
     */
    public String getJsp() {
        return jsp;
    }

    /**
     * @param jsp
     *            the jsp to set
     */
    public void setJsp(String jsp) {
        this.jsp = jsp;
    }

    /**
     * @return the _default
     */
    public boolean isDefault() {
        return _default;
    }

    /**
     * @param _default
     *            the _default to set
     */
    public void setDefault(boolean _default) {
        this._default = _default;
    }

}
