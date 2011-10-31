<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pg" uri="http://jsptags.com/tags/navigation/pager" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>


<%@ attribute name="record"
			  type="org.gusdb.wdk.model.jspwrap.RecordBean"
              required="true"
              description="The record instance"
%>

<c:set var="recordClass" value="${record.recordClass}" />
<c:set var="primaryKey" value="${record.primaryKey}" />
<c:set var="pkUrl" value="" />
<c:forEach items="${primaryKey.values}" var="item">
  <c:set var="pkUrl" value="${pkUrl}&${item.key}=${item.value}" />
</c:forEach>

<div class="Workspace">

<div class="h2center">${recordClass.type}: ${primaryKey}</div>

<%-- display view list --%>
<div id="Record_Views" class="ui-tabs ui-widget ui-widget-content ui-corner-all">
  <c:set var="currentView" value="${recordClass.defaultRecordView.name}" />
  <c:set var="views" value="${recordClass.recordViews}" />
  <c:set var="index" value="${0}" />

  <ul id="record_views" class="ui-tabs-nav ui-helper-reset ui-helper-clearfix ui-widget-header ui-corner-all">
    <c:forEach items="${views}" var="item">
      <c:set var="view" value="${item.value}" />
      <c:if test="${view.name == currentView}">
        <c:set var="selectedTab" value="${index}" />
      </c:if>
      <li class="ui-state-default ui-corner-top">
        <a title="Feature_Pane" 
           href="<c:url value='/showRecordFeature.do?name=${recordClass.fullName}&view=${view.name}${pkUrl}' />"
        >${view.display}</a>
      </li>
      <c:set var="index" value="${index + 1}" />
    </c:forEach>
  </ul>

</div> <!-- END OF Record_Views -->

<div id="Feature_Pane"> </div>

<script>
  $(function() {
    $( "#Record_Views" ).tabs({ selected : ${selectedTab} });
  });
</script>

</div> <!-- END of .Workspace -->
