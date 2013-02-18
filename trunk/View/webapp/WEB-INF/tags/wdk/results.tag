<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pg" uri="http://jsptags.com/tags/navigation/pager" %>
<%@ taglib prefix="imp" tagdir="/WEB-INF/tags/imp" %>

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

<c:set var="wdkModel" value="${applicationScope.wdkModel}" />
<c:set var="wdkUser" value="${sessionScope.wdkUser}" />
<c:set var="projectId" value="${wdkModel.projectId}" />
<c:set var="dispModelName" value="${wdkModel.displayName}" />
<c:set var="wdkAnswer" value="${step.answerValue}"/>
<c:set var="recordClass" value="${wdkAnswer.question.recordClass}" />
<c:set var="recordName" value="${recordClass.fullName}" />
<c:set var="recHasBasket" value="${recordClass.useBasket}" />

<c:set var="recordName" value="${wdkStep.recordClass.displayNamePlural}"/>


<!-- ================ TAG SHARED BY BASKET AND OPENED TABS =============== -->
<!-- handle empty result set situation -->
<c:choose>
  <c:when test='${strategy eq null and wdkUser.guest and wdkAnswer.resultSize eq 0}'>
    Please login to use the basket
  </c:when>
  <c:when test='${strategy eq null and wdkAnswer.resultSize eq 0}'>
    Basket Empty
  </c:when>
  <c:otherwise>

<!-- ================ FILTERS DEFINED IN MODEL.XML =============== -->

<c:if test="${strategy != null}">
  <imp:filterLayouts strategyId="${strategy.strategyId}" 
                     stepId="${step.stepId}"
                     answerValue="${wdkAnswer}" />
</c:if>


<!-- ================ RESULTS TITLE AND LINKS TO BASKET AND DOWNLOADS   =============== -->

<imp:resultSummary strategy="${wdkStrategy}" step="${wdkStep}"/>



<!-- ================ SUMMARY VIEWS (EXTRA TABS DEFINED IN MODEL.XML)  =============== -->
<%-- display view list --%>
<script>
$(function() {
  wdk.resultsPage.configureSummaryViews(this);
});
</script>


<div id="Summary_Views" class="Summary_Views" strategy="${strategy.strategyId}" step="${step.stepId}"
     updateUrl="<c:url value='/processSummaryView.do' />">
  <c:set var="question" value="${wdkStep.question}" />
  <c:set var="views" value="${question.summaryViews}" />
  <jsp:setProperty name="wdkUser" property="currentQuestion" value="${question}" />
  <c:set var="currentView" value="${wdkUser.currentSummaryView.name}" />
  
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
      <li id="${view.name}">
        <a href="<c:url value='/showSummaryView.do?strategy=${wdkStrategy.strategyId}&step=${wdkStep.stepId}&view=${view.name}' />"
           title="${view.description}"
        >${view.display} <span> </span></a>
      </li>
    </c:forEach>
  </ul>
</div>

  </c:otherwise>
</c:choose>
