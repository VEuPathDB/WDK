<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pg" uri="http://jsptags.com/tags/navigation/pager" %>
<%@ taglib prefix="imp" tagdir="/WEB-INF/tags/imp" %>


<%@ attribute name="record"
			  type="org.gusdb.wdk.model.jspwrap.RecordBean"
              required="true"
              description="The record instance"
%>

<%@ attribute name="recordSubtitle"
              required="false"
              description="Text that is displayed as a subtitle"
%>

<c:set var="wdkUser" value="${sessionScope.wdkUser}" />
<c:set var="recordClass" value="${record.recordClass}" />
<c:set var="primaryKey" value="${record.primaryKey}" />
<c:set var="pkUrl" value="" />
<c:forEach items="${primaryKey.values}" var="item">
  <c:set var="pkUrl" value="${pkUrl}&${item.key}=${item.value}" />
</c:forEach>

<div class="Workspace">

<div id="record-title" class="h2center">${recordClass.displayName}: ${primaryKey}</div>
<c:if test="${recordSubtitle ne ''}">
  <div id="record-subtitle" class="h3center">${recordSubtitle}</div>
</c:if>

<div id="basket-control">
  <imp:recordPageBasketIcon />
</div>

<%-- display view list --%>
<div id="Record_Views" class="ui-tabs ui-widget ui-widget-content ui-corner-all">
  <c:set var="views" value="${recordClass.recordViews}" />
  <jsp:setProperty name="wdkUser" property="currentRecordClass" value="${recordClass}" />
  <c:set var="currentView" value="${wdkUser.currentRecordView.name}" />

  <%-- get the index of the current view --%>
  <c:set var="selectedTab" value="${0}" />
  <c:set var="index" value="${0}" />
  <c:forEach items="${views}" var="item">
      <c:if test="${item.key == currentView}">
        <c:set var="selectedTab" value="${index}" />
      </c:if>
      <c:set var="index" value="${index + 1}" />
  </c:forEach>

  <ul currentTab="${selectedTab}">
    <c:forEach items="${views}" var="item">
      <c:set var="view" value="${item.value}" />
      <li>
        <a href="<c:url value='/showRecordView.do?name=${recordClass.fullName}&view=${view.name}${pkUrl}' />"
        >${view.display} <span> </span></a>
      </li>
    </c:forEach>
  </ul>

</div> <!-- END OF Record_Views -->

<script>
  $(function() {
    var currentTab = parseInt(jQuery("#Record_Views > ul").attr("currentTab"));
    jQuery( "#Record_Views" )
        .tabs({
            active : currentTab,

            beforeLoad: function(event, ui) {

              if (ui.tab.data("loaded")) {
                event.preventDefault();
                return;
              }

              ui.tab.find("span").append('<img style="margin-left:4px; position: relative; top:2px;" src="wdk/images/filterLoading.gif"/>');

              ui.jqXHR.success(function() {
                ui.tab.data("loaded", true);
                ui.tab.find("img").remove();
              });
            },

            load: function(event, ui) {
              wdk.event.publish("recordload", ui.panel);
            }
        });
  });
</script>

</div> <!-- END of .Workspace -->
