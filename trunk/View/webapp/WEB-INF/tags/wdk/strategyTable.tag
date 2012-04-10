<%@ taglib prefix="imp" tagdir="/WEB-INF/tags/imp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>

<%@ attribute name="strategies"
              type="java.util.List"
              required="true"
              description="List of Strategy objects"
%>
<%@ attribute name="wdkUser"
              type="org.gusdb.wdk.model.jspwrap.UserBean"
              required="true"
              description="Current User object"
%>
<%@ attribute name="prefix"
              type="java.lang.String"
              required="false"
              description="Text to add before 'Strategy' in column header"
%>
<!-- strategyTable.tag -->
<c:set var="scheme" value="${pageContext.request.scheme}" />
<c:set var="serverName" value="${pageContext.request.serverName}" />
<c:set var="request_uri" value="${requestScope['javax.servlet.forward.request_uri']}" />
<c:set var="request_uri" value="${fn:substringAfter(request_uri, '/')}" />
<c:set var="request_uri" value="${fn:substringBefore(request_uri, '/')}" />
<c:set var="exportBaseUrl" value = "${scheme}://${serverName}/${request_uri}/im.do?s=" />

<table class="datatables" border="0" cellpadding="5" cellspacing="0">
  <thead>
  <tr class="headerrow">
    <th scope="col" style="width: 25px;">&nbsp;</th>
    <th class="sortable" scope="col">
      <c:if test="${prefix != null}">${prefix}&nbsp;</c:if>Strategies&nbsp;(${fn:length(strategies)})
    </th>
    <th scope="col" style="width: 3em;">&nbsp;</th>
    <th scope="col" style="width: 4em">&nbsp;</th>
    <th scope="col" style="width: 9em">&nbsp;</th>
    <th class="sortable" scope="col" style="width: 5em;text-align:center">Created</th>
    <th class="sortable" scope="col" style="width: 5em;text-align:center">
      <c:choose>
        <c:when test="${prefix == 'Saved'}">Saved At</c:when>
        <c:otherwise>Last Modified</c:otherwise>
      </c:choose>
    </th>
    <th class="sortable" scope="col" style="width: 4em" title="It refers to the Website Version. See the Version number of this current release on the top left side of the header, on the right of the site name">Version</th>
    <th class="sortable" scope="col" style="width: 4em;text-align:right">Size</th>
    <th>&nbsp;&nbsp;</th>
  </tr>
  </thead>


  <tbody <c:if test="${prefix == 'Unsaved'}">class="unsaved-strategies-body"</c:if>>
  <c:set var="i" value="0"/>
  <%-- begin of forEach strategy in the category --%>
  <c:forEach items="${strategies}" var="strategy">
    <c:set var="strategyId" value="${strategy.strategyId}"/>
   <tr>
      <td scope="row"><input type=checkbox id="${strategyId}" onclick="updateSelectedList()"/></td>
      <%-- need to see if this strategy id is in the session. --%>
      <c:set var="active" value=""/>
      <c:set var="openedStrategies" value="${wdkUser.activeStrategyIds}"/>
      <c:forEach items="${openedStrategies}" var="activeId">
        <c:if test="${strategyId == activeId}">
          <c:set var="active" value="true"/>
        </c:if>
      </c:forEach>

<%--
      <td>
        <img id="img_${strategyId}" class="plus-minus plus" src="<c:url value='/wdk/images/sqr_bullet_plus.png'/>" alt="" onclick="toggleSteps2(${strategyId})"/>
      </td>
--%>

      <c:set var="dispNam" value="${strategy.name}"/>

      <td>
        <div id="text_${strategyId}">
          <span <c:choose>
		<c:when test="${active}">style="font-weight:bold;cursor:pointer" title="Click to go to the graphical display (Run tab)"</c:when>
		<c:otherwise> style="cursor:pointer" title="Click to open this strategy in the strategy graphical display (Run tab)" </c:otherwise>
		</c:choose>
		 onclick="openStrategy('${strategyId}')">${dispNam}</span><c:if test="${!strategy.isSaved}">*</c:if><c:if test="${!strategy.valid}">&nbsp;&nbsp;&nbsp;<img src="<c:url value='/wdk/images/invalidIcon.png'/>" width="12"/></c:if>
        </div> 

      </td>

      <td nowrap>
        <div id="activate_${strategyId}">
          <c:choose>
            <c:when test="${!active}">
              <span style="display:none">Open</span>
              <input type='button' value='Open' onclick="openStrategy('${strategyId}')" />
            </c:when>
            <c:otherwise>
              <span style="display:none">Close</span>
              <input type='button' value='Close' onclick="closeStrategy('${strategyId}', true)" />
            </c:otherwise>
          </c:choose>
        </div>
      </td>

      <td>
         <input type='button' value='Download' onclick="downloadStep('${strategy.latestStepId}')" />
      </td>
      <td nowrap>
         <c:set var="saveAction" value="showHistSave(this, '${strategyId}', true);"/>
         <c:set var="shareAction" value="showHistShare(this, '${strategyId}', '${exportBaseUrl}${strategy.importId}');" />
         <c:if test="${!strategy.isSaved}">
           <c:set var="shareAction" value="showHistSave(this, '${strategyId}', true,true);" />
         </c:if>
         <c:if test="${wdkUser.guest}">
           <c:set var="saveAction" value="popLogin();"/>
           <c:set var="shareAction" value="popLogin();"/>
         </c:if>
         <select id="actions_${strategyId}" onchange="eval(this.value);this[0].selected='true';">
            <option value="return false;">---More actions---</option>
            <option value="showHistSave(this, '${strategyId}', false)">Rename</option>
            <option value="copyStrategy('${strategyId}', true);">Copy</option>
            <option value="${saveAction}">Save As</option>
            <option value="${shareAction}">Share</option>
      <!--      <option value="deleteStrategy(${strategyId}, true)">Delete</option> -->
<!-- I think we should remove the delete option here -->
      <option value="handleBulkStrategies('delete',${strategyId})">Delete</option>
         </select>
      </td>
      <td nowrap style="padding:0 2px 0 2px;">${strategy.createdTimeFormatted}</td>
      <td nowrap style="padding:0 2px 0 2px;">${strategy.lastModifiedTimeFormatted}</td>
<%--       <td nowrap  style="padding:0 2px;text-align:right">${strategy.lastRunTimeFormatted}</td> --%>
      <td nowrap style="text-align:center">
        <c:choose>
          <c:when test="${strategy.version == null || strategy.version eq ''}">${wdkModel.version}</c:when>
          <c:otherwise>${strategy.version}</c:otherwise>
        </c:choose>
      </td>
      <td nowrap style="text-align:right">${strategy.estimateSize}</td>
      <td>&nbsp;&nbsp;</td>
    </tr>

    <c:set var="i" value="${i+1}"/>
  </c:forEach>
  <!-- end of forEach strategy in the category -->
  </tbody>
</table>
<!-- end strategyTable.tag -->
