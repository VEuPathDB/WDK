<%@ taglib prefix="imp" tagdir="/WEB-INF/tags/imp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>


<%@ attribute name="model"
             type="org.gusdb.wdk.model.jspwrap.WdkModelBean"
             required="false"
             description="Wdk Model Object for this site"
%>

<%@ attribute name="user"
              type="org.gusdb.wdk.model.jspwrap.UserBean"
              required="false"
              description="Currently active user object"
%>

<c:set var="releaseDate" value="${applicationScope.wdkModel.releaseDate}" />
<c:set var="inputDateFormat" value="dd MMMM yyyy HH:mm"/>
<fmt:setLocale value="en-US"/>
<fmt:parseDate pattern="${inputDateFormat}" var="rlsDate" value="${releaseDate}"/> 
<fmt:formatDate var="releaseDate_formatted" value="${rlsDate}" pattern="d MMM yyyy"/>

<!--strategyHistory.tag-->
<c:set var="unsavedStrategiesMap" value="${user.unsavedStrategiesByCategory}"/>
<c:set var="savedStrategiesMap" value="${user.savedStrategiesByCategory}"/>
<c:set var="invalidStrategies" value="${user.invalidStrategies}"/>
<c:set var="modelName" value="${model.name}"/>

<!-- decide whether strategy history is empty -->
<span style="display:none" id="totalStrategyCount">${user.strategyCount}</span>
<c:choose>
  <c:when test="${user == null || user.strategyCount == 0}">
  <div style="font-size:120%;line-height:1.2em;text-indent:10em;padding:0.5em">You have no search strategies in your history. <p style="text-indent:10em;">Maybe you need to login? Otherwise please run a search to start a strategy.</p></div>
  </c:when>
  <c:otherwise>

    <div style="border:0;" id="history-menu" class="tabs">
      <ul>

      <!-- the order of tabs is determined in apicommonmodel.xml -->
      <c:forEach items="${unsavedStrategiesMap}" var="strategyEntry">
        <c:set var="type" value="${strategyEntry.key}"/>
        <c:set var="unsavedStratList" value="${strategyEntry.value}"/>
        <c:set var="savedStratList" value="${savedStrategiesMap[type]}" />
        <c:set var="totalStratsCount" value="${fn:length(savedStratList) + fn:length(unsavedStratList)}"/>
        <c:if test="${fn:length(unsavedStratList) > 0 || fn:length(savedStratList) > 0}">
          <c:choose>
            <c:when test="${fn:length(unsavedStratList) > 0}">
              <c:set var="strat" value="${unsavedStratList[0]}" />
            </c:when>
            <c:otherwise>
              <c:set var="strat" value="${savedStratList[0]}" />
            </c:otherwise>
          </c:choose>
          <c:set var="recDispName" value="${strat.recordClass.displayNamePlural}"/>
          <c:set var="recTabName" value="${fn:replace(recDispName, ' ', '_')}"/>
          <li id="tab_${recTabName}" data-name="${recTabName}">
            <a href="showQueryHistory.do?recordType=${type}">
              ${recDispName} (${totalStratsCount}) <span></span></a>
          </li>
        </c:if>
      </c:forEach>

      <c:if test="${fn:length(invalidStrategies) > 0}">
        <li>
          <a id="tab_invalid" onclick="wdk.history.displayHist('invalid')"
           href="javascript:void(0)">Invalid&nbsp;Strategies</a></li>
      </c:if>

      </ul>

    </div>
  </c:otherwise>
</c:choose>

