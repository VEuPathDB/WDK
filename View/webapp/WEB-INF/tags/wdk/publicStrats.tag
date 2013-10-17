<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="2.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:c="http://java.sun.com/jsp/jstl/core"
    xmlns:fmt="http://java.sun.com/jsp/jstl/fmt"
    xmlns:fn="http://java.sun.com/jsp/jstl/functions"
    xmlns:imp="urn:jsptagdir:/WEB-INF/tags/imp">
  <fmt:setLocale value="en-US"/> <!-- required for date parsing when client (e.g. curl) does not send locale -->
  <c:set var="project" value="${applicationScope.wdkModel.properties['PROJECT_ID']}"/>
  <c:set var="examplesAuthor" value="${applicationScope.wdkModel.model.exampleStratsAuthor.name}"/>
  <div>
    <!-- To show sample strategies in their own table, use tag imp:sampleStrategies -->
    <span id="publicStrategyCount" style="display:none">${numValidPublicStrats}</span>
    <div class="h2center" style="padding-top:12px">Examples &amp;amp; Public Strategies</div>
    <div style="text-align:center;margin:5px"><em>To make one of your strategies visible to the community, go to the All tab and click its Public checkbox.</em></div>
    <c:if test="${not empty examplesAuthor}">
      <span id="checkboxMarkup" style="float:right; line-height:1.4em; margin-top:9px">
        <!--<input type="checkbox" onclick="wdk.publicStrats.toggleSampleOnly(this,'${examplesAuthor}')"/>
        <span>View ${examplesAuthor} Strategies Only</span><br/>
        <a href="javascript:void(0)" onclick="wdk.publicStrats.sortSampleToTop()">Push ${examplesAuthor} Strategies To The Top</a>-->
        <input id="sampleToTopCheckbox" type="checkbox" onclick="wdk.publicStrats.toggleSampleToTop(this)"/><span>Set ${examplesAuthor} Strategies On Top</span>
      </span>
    </c:if>
    <table class="datatables" style="width:100%; margin:0 auto 0 auto" border="0" cellpadding="5" cellspacing="0">
      <thead>
        <tr class="headerrow">
          <th class="sortable" scope="col">Is Example</th>
          <th class="sortable" scope="col" style="min-width:16em;">Strategies (${numValidPublicStrats})</th>
          <th class="sortable" style="width:9em;" scope="col">Returns</th>
          <th scope="col">Description</th>
          <th class="sortable" scope="col">Author</th>
          <th class="sortable" scope="col">Institution</th>
          <th class="sortable" style="width:9em;" scope="col">Last Modified</th>
        </tr>
      </thead>
      <tbody>
        <c:forEach items="${publicStrats}" var="strategy">
          <!-- do not display invalid public strategies -->
          <c:if test="${strategy.valid}">
            <c:set var="strategyId" value="${strategy.strategyId}"/>
            <c:set var="strategyDesc"><c:out value="${strategy.description}"/></c:set>
            <c:set var="displayName" value="${strategy.name}"/>
            <c:set var="isExample" value="${strategy.user.displayName eq examplesAuthor ? '1' : '0'}"/>
            <c:set var="addtlClass" value="${isExample eq '1' ? ' exampleStratRow' : ''}"/>
            <tr id="public_strat_${strategyId}"
                class="strategy-data${addtlClass}"
                data-back-id="${strategyId}"
                data-name="${strategy.name}"
                data-description="${strategyDesc}"
                data-saved="${strategy.isSaved}"
                data-step-id="${strategy.latestStepId}">
              <td>${isExample}</td>
              <td>
                <div id="text_${strategyId}">
                  <span title="View and modify this strategy as your own">
                    <a href="im.do?s=${strategy.signature}">${displayName}</a>
                  </span>
                </div>
              </td>
              <td nowrap="nowrap">${strategy.latestStep.question.recordClass.displayNamePlural}</td>
              <td class="strategy_description">
                <div class="full" title="Click to view entire description"
                     onclick="wdk.history.showDescriptionDialog(this, false, true, false);">
                  <c:set var="maxDescriptionLen" value="70"/>
                  <c:choose>
                    <c:when test="${fn:length(strategy.description) gt maxDescriptionLen}">
                      <c:set var="description" value="${fn:substring(strategy.description, 0, (maxDescriptionLen - 3))}"/>
                      <c:set var="continuation" value="..."/>
                    </c:when>
                    <c:otherwise>
                      <c:set var="description" value="${strategy.description}"/>
                      <c:set var="continuation" value=""/>
                    </c:otherwise>
                  </c:choose>
                  ${description}${continuation}
                </div>
              </td>
              <td nowrap="nowrap">${strategy.user.displayName}</td>
              <td nowrap="nowrap">${strategy.user.organization}</td>
              <fmt:formatDate var="modifiedTimeFormatted" value="${strategy.lastModifiedTime}" pattern="yyyy-MM-dd"/>
              <td nowrap="nowrap" style="padding:0 2px 0 2px;">${modifiedTimeFormatted}</td>
            </tr>
          </c:if>
        </c:forEach>
      </tbody>
    </table>
  </div>
</jsp:root>