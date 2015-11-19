<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="imp" tagdir="/WEB-INF/tags/imp" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<a style="float:right;" href="javascript:void(0)" onclick="wdk.strategy.view.closeInvalidText(this)"><imp:image src="wdk/images/close.gif"/></a>
<br>
<p style="font-size:95%">
This strategy contains steps that are outdated.  <br>These steps are marked with a red <span style="font-size:120%;color:red;">X</span>.  <br>Click on the Edit button that appears as you mouseover a step, to open the dialog and click on "<b>Revise</b>" to update the search parameters.

</p>

<%-- add link to a site tutorial on revising invalid searches; using the id defined in tutorials.xml for this tutorial --%>
<imp:linkTutorial id="140" /> 

<p style="font-size:95%">
<br>
(Warning: if a step contains an <b>obsolete</b> search, you will no be offered the option to revise the step; only to delete it. Currently such a strategy cannot be modified: you will have to recreate the strategy. Please contact us if we can help with this.)
</p>
