<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pg" uri="http://jsptags.com/tags/navigation/pager" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>


<%@ attribute name="strategy"
              type="org.gusdb.wdk.model.jspwrap.StrategyBean"
              required="true"
              description="Strategy bean we are looking at"
%>


<%@ attribute name="step"
              type="org.gusdb.wdk.model.jspwrap.StepBean"
              required="true"
              description="Step bean we are looking at"
%>


<c:set var="projectId" value="${applicationScope.wdkModel.projectId}" />
<c:set var="dispModelName" value="${applicationScope.wdkModel.displayName}" />
<c:set var="wdkAnswer" value="${step.answerValue}"/>
<c:set var="recordClass" value="${wdkAnswer.question.recordClass}" />
<c:set var="recordName" value="${recordClass.fullName}" />
<c:set var="recHasBasket" value="${recordClass.useBasket}" />


<jsp:useBean id="typeMap" class="java.util.HashMap"/>
<c:set target="${typeMap}" property="singular" value="${wdkStep.displayType}"/>
<wdk:getPlural pluralMap="${typeMap}"/>
<c:set var="type" value="${typeMap['plural']}"/>

<c:if test="${strategy != null}">
  <wdk:filterLayouts strategyId="${strategy.strategyId}" 
                     stepId="${step.stepId}"
                     answerValue="${wdkAnswer}" />
</c:if>

<!-- handle empty result set situation -->
<c:choose>
  <c:when test='${strategy != null && wdkAnswer.resultSize == 0}'>
	No results are retrieved
  </c:when>
  <c:when test='${strategy == null && wdkUser.guest && wdkAnswer.resultSize == 0}'>
    Please login to use the basket
  </c:when>
  <c:when test='${strategy == null && wdkAnswer.resultSize == 0}'>
    Basket Empty
  </c:when>
  <c:otherwise>

<table width="100%"><tr>
<td class="h3left" style="vertical-align:middle;padding-bottom:7px;">
    <c:if test="${strategy != null}">
        <span id="text_strategy_number">${strategy.name}</span> 
        - step <span id="text_step_number">${strategy.length}</span> - 
    </c:if>
    <span id="text_step_count">${wdkAnswer.resultSize}</span> <span id="text_data_type">${type}</span>
</td>

<td  style="vertical-align:middle;text-align:right;white-space:nowrap;">
  <div style="float:right">
   <c:set var="r_count" value="${wdkAnswer.resultSize} ${type}" />
   <c:if test="${strategy != null}">
    <c:choose>
      <c:when test="${wdkUser.guest}">
        <c:set var="basketClick" value="popLogin();setFrontAction('basketStep');" />
      </c:when>
      <c:otherwise>
        <c:set var="basketClick" value="updateBasket(this, '${step.stepId}', '0', '${modelName}', '${recordName}');" />
      </c:otherwise>
    </c:choose>
    <c:if test="${recHasBasket}"><a id="basketStep" style="font-size:120%" href="javascript:void(0)" onClick="${basketClick}"><b>Add ${r_count} to Basket</b></a>&nbsp;|&nbsp;</c:if>
   </c:if>
    <a style="font-size:120%" href="downloadStep.do?step_id=${step.stepId}"><b>Download ${r_count}</b></a>
  <c:if test="${!empty sessionScope.GALAXY_URL}">
    &nbsp;|&nbsp;<a href="downloadStep.do?step_id=${step.stepId}&wdkReportFormat=tabular"><b>SEND TO GALAXY</b></a>
  </c:if>
  </div>
</td>
</tr></table>


<%-- display view list --%>
<script>
$(function() {
  configureSummaryViews(this);
});
</script>


<div id="Summary_Views" strategy="${strategy.strategyId}" step="${step.stepId}"
     updateUrl="<c:url value='/updateResult.do' />">
  <c:set var="question" value="${wdkStep.question}" />
  <c:set var="currentView" value="${question.defaultSummaryView.name}" />
  <c:set var="views" value="${question.summaryViews}" />
  <c:set var="index" value="${0}" />

  <ul>
    <c:forEach items="${views}" var="item">
      <c:set var="view" value="${item.value}" />
      <c:if test="${view.name == currentView}">
        <c:set var="selectedTab" value="${index}" />
      </c:if>
      <li id="${view.name}">
        <a href="<c:url value='/showResult.do?strategy=${wdkStrategy.strategyId}&step=${wdkStep.stepId}&view=${view.name}' />"
        >${view.display} <span> </span></a>
      </li>
      <c:set var="index" value="${index + 1}" />
    </c:forEach>
  </ul>
</div>

  </c:otherwise>
</c:choose>
