<%@ taglib prefix="imp" tagdir="/WEB-INF/tags/imp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%-- we need the header only for the css and js --%>
<imp:header title="${site}.org :: Support"
                 banner="Support"
                 parentDivision="${site}"
                 parentUrl="/home.jsp"
                 divisionName="Generic"
                 division="help"/>


<style type="text/css">
#contentcolumn2 {
   min-width:0;
}
</style>

<c:set var="releaseDate" value="${applicationScope.wdkModel.releaseDate}" />
<c:set var="inputDateFormat" value="dd MMMM yyyy HH:mm"/>
<fmt:setLocale value="en-US"/><%-- req. for date parsing when client browser (e.g. curl) doesn't send locale --%>
<fmt:parseDate pattern="${inputDateFormat}" var="rlsDate" value="${releaseDate}"/> 
<%-- http://java.sun.com/j2se/1.5.0/docs/api/java/text/SimpleDateFormat.html --%>
<fmt:formatDate var="releaseDate_formatted" value="${rlsDate}" pattern="d MMM yyyy"/>
  

<div style="font-size:20px;font-family:Arial;text-align:right">
<a href="javascript:window.close()">Close (X)</a>  
</div>

<h3>How new data, new annotation or search changes, might affect your strategies</h3>

<div id="cirbulletlist" style="font-size:14px;font-family:Arial">
<br>
About every 2 months we release new data in our databases and this might affect your strategies.<br>
(Last release ${releaseDate_formatted}.)
<br><br>
<ul>
<li>The set of IDs in any step in your strategies, might change due to new data or annotation. Please be aware that we do not warn you when this happens, and that we cannot recover your old result. <br>Only IDs stored in <i>Favorites</i> will stay unchanged.
<br><br>
<li>Also, we might have updated some of the searches in your steps: a parameter could have been modified or removed or we added a new parameter in the search. When this happens we will mark your strategy as "outdated" with the red icon (<img src="<c:url value="/wdk/images/invalidIcon.png"/>" width="12"/>). When you open the strategy you will see one or more steps crossed out by two red  lines. When you click on the step, you will be given the choice to revise and rerun the step.
</ul>
<br>
The EuPathDB Team
</div>


<imp:footer/>


