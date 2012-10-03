<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="imp" tagdir="/WEB-INF/tags/imp" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<a style="float:right;" href="javascript:void(0)" onclick="closeInvalidText(this)"><img src="${pageContext.request.contextPath}/wdk/images/close.gif"></a>
<br>
<p style="font-size:95%">
This strategy contains steps that are outdated.  <br>These steps are marked with a red <font size="120%" color="red">X</font>.  <br>Click on the step name to open the dialog and click on "<b>Revise</b>" to update the search parameters.
</p>

<%-- add link to a site tutorial on revising invalid searches; using the id defined in tutorials.xml for this tutorial --%>
<imp:linkTutorial id="140" /> 

