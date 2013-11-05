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

<c:set var="checkboxTitle" value="Check this box to make your strategy visible to the community under the 'Public' tab."/>

<!-- adding/removing columns: update datatable config in strategyHistory.jsp -->
<table class="datatables" border="0" cellpadding="5" cellspacing="0">
  <thead>
  <tr class="headerrow">
    <th scope="col" style="width:25px;">&nbsp;</th>
    <th class="sortable" scope="col" style="min-width:16em;">
      <c:if test="${prefix != null}">${prefix}&nbsp;</c:if>Strategies&nbsp;(${fn:length(strategies)})
    </th>
    <%-- <th class = "sortable" style="width:5em;" scope="col">#Steps</th> --%>
    <th scope="col">Description</th>
    <th scope="col" style="width:12em;">Actions</th>
    <th class="sortable" style="width:9em;" scope="col">Created</th>
    <th class="sortable" style="width:9em;" scope="col">
      <c:choose>
        <c:when test="${prefix == 'Saved'}">Saved At</c:when>
        <c:otherwise>Last Modified</c:otherwise>
      </c:choose>
    </th>
    <th class="sortable" scope="col" style="width: 6em" title="It refers to the Website Version. See the Version number of this current release on the top left side of the header, on the right of the site name">Version</th>
    <th class="sortable" scope="col" style="width: 4em;text-align:right">Size</th>
    <th class="sortable" scope="col" style="width: 5em" title="${checkboxTitle}">Public</th>
  </tr>
  </thead>


  <tbody <c:if test="${prefix == 'Unsaved'}">class="unsaved-strategies-body"</c:if>>
  <c:set var="i" value="0"/>
  <%-- begin of forEach strategy in the category --%>
  <c:forEach items="${strategies}" var="strategy">
    <c:set var="strategyId" value="${strategy.strategyId}"/>
    <tr id="strat_${strategyId}"
        class="strategy-data"
        data-back-id="${strategyId}"
        data-name="${strategy.name}"
        data-description="<c:out value="${strategy.description}"/>"
        data-saved="${strategy.isSaved}"
        data-step-id="${strategy.latestStepId}"
        data-is-public="${strategy.isPublic}"
        data-valid="${strategy.valid}">
      <td scope="row"><input class="strat-selector-box" type=checkbox id="${strategyId}" onclick="wdk.history.updateSelectedList()"/></td>
      <%-- need to see if this strategy id is in the session. --%>
      <c:set var="active" value=""/>
      <c:set var="openedStrategies" value="${wdkUser.activeStrategyIds}"/>
      <c:forEach items="${openedStrategies}" var="activeId">
        <c:if test="${strategyId == activeId}">
          <c:set var="active" value="true"/>
        </c:if>
      </c:forEach>

<%--
      <td><imp:image id="img_${strategyId}" class="plus-minus plus" src="/wdk/images/sqr_bullet_plus.png" alt="" onclick="wdk.history.toggleSteps2(${strategyId})"/></td>
--%>

      <c:set var="dispNam" value="${strategy.name}"/>

      <td>
        <c:choose>
          <c:when test="${active}"><c:set var="openTitle" value="This strategy is already opened in the Strategy panel (bold); click to go!"/></c:when>
          <c:otherwise><c:set var="openTitle" value="Click to open this strategy in the Strategy panel (Browse tab)"/></c:otherwise>
        </c:choose>
        <div id="text_${strategyId}" 
             style="cursor:pointer" 
             onclick="wdk.strategy.controller.openStrategy('${strategyId}')"
             title="${openTitle}">
          <c:set var="extraActiveStyle" value="${active ? 'font-weight:bold;' : ''}"/>
          <span style="${extraActiveStyle}">
            <c:out value="${dispNam}"/>
          </span>
          <c:if test="${!strategy.isSaved}">*</c:if>
          <c:if test="${!strategy.valid}">&nbsp;&nbsp;&nbsp;<imp:image title="This strategy has one or more steps that need to be revised, due to release updates; click to revise!" src="/wdk/images/invalidIcon.png" width="12"/></c:if>
        </div> 

      </td>

      <%-- <td style="text-align:center">${strategy.length}</td> --%>

      <td class="strategy_description">
      <c:choose>
        <c:when test="${wdkUser.guest}">
          <div class="unsaved" title="Click to save and add description" onclick="wdk.user.login();">${not empty strategy.description ? strategy.description : 'Click to save and add a description'}</div>
        </c:when>
        <c:otherwise>
          <c:choose>
            <c:when test="${!strategy.isSaved}">
              <div class="unsaved" title="Click to save and add description" onclick="wdk.history.showUpdateDialog(this, true, true);">${not empty strategy.description ? strategy.description : 'Save to add a description'}</div>
            </c:when>
            <c:when test="${empty strategy.description}">
              <div class="empty" title="Click to add a description" onclick="wdk.history.showUpdateDialog(this, false, true);">Click to add a description</div>
            </c:when>
            <c:otherwise>
              <div class="full" title="Click to view entire description" onclick="wdk.history.showDescriptionDialog(this, false, true, true);"><c:out value="${strategy.description}"/></div>
            </c:otherwise>
          </c:choose>
        </c:otherwise>
      </c:choose>
      </td>

      <td nowrap>
         <c:set var="saveAction" value="wdk.history.showUpdateDialog(this, true, true);"/>
         <c:set var="shareAction" value="wdk.history.showHistShare(this, '${strategyId}', '${exportBaseUrl}${strategy.importId}');" />
         <c:if test="${!strategy.isSaved}">
           <c:set var="shareAction" value="if (confirm('Before you can share your strategy, you need to save it. Would you like to do that now?')) { ${saveAction} }" />
         </c:if>
         <c:if test="${wdkUser.guest}">
           <c:set var="saveAction" value="wdk.user.login();"/>
           <c:set var="shareAction" value="wdk.user.login();"/>
         </c:if>
         <select id="actions_${strategyId}" onchange="eval(this.value);this[0].selected='true';">
            <option value="return false;">---Actions---</option>
            <c:choose>
              <c:when test="${!active}">
                <option value="wdk.strategy.controller.openStrategy('${strategyId}')">Open</option>
              </c:when>
              <c:otherwise>
                <option value="wdk.strategy.controller.closeStrategy('${strategyId}', true)">Close</option>
              </c:otherwise>
            </c:choose>
            <option value="wdk.history.downloadStep('${strategy.latestStepId}')">Download</option>
            <option value="wdk.history.showUpdateDialog(this, false, true)">Rename</option>
            <c:if test="${strategy.isSaved}">
              <option value="wdk.history.showUpdateDialog(this, false, true)">
              <c:choose>
                <c:when test="${empty strategy.description}">
                  Add description
                </c:when>
                <c:otherwise>
                  Edit description
                </c:otherwise>
              </c:choose>
              </option>
            </c:if>
            <option value="wdk.strategy.controller.copyStrategy('${strategyId}', true);">Duplicate</option>
            <option value="${saveAction}">Save As</option>
            <option value="${shareAction}">Share</option>
            <option value="wdk.history.handleBulkStrategies('delete',${strategyId})">Delete</option>
         </select>
      </td>
      <td nowrap style="padding:0 2px 0 2px;">${fn:substring(strategy.createdTimeFormatted, 0, 10)}</td>
      <td nowrap style="padding:0 2px 0 2px;">${fn:substring(strategy.lastModifiedTimeFormatted, 0, 10)}</td>
      <td nowrap style="text-align:center">${strategy.version}</td>
      <td nowrap style="text-align:right">${strategy.estimateSize}&nbsp;</td>
      
      <c:set var="checkboxTitle" value="Check this box to make your strategy visible to the community under the 'Public' tab."/>
      <c:set var="disabledProp" value=""/>
      <c:choose>
        <c:when test="${!strategy.valid}">
          <c:set var="disabledProp">disabled="disabled"</c:set>
          <c:set var="checkboxTitle" value="This strategy must be revised before it can be made public.  It is invalid due to release updates."/>
        </c:when>
        <c:when test="${wdkUser.guest}">
          <c:set var="makePublicAction" value="$(this).prop('checked',false); wdk.user.login();"/>
          <c:set var="checkboxTitle" value="You must log in before making any of your strategies public.  Click here to do so."/>
        </c:when>
        <c:when test="${strategy.isSaved}">
          <c:choose>
            <c:when test="${empty strategy.description}">
              <c:set var="makePublicAction" value="$(this).prop('checked',false); wdk.history.showUpdateDialog(this, false, true, true);"/>
            </c:when>
            <c:otherwise>
              <c:set var="makePublicAction" value="wdk.publicStrats.togglePublic(this,'${strategyId}')"/>
            </c:otherwise>
          </c:choose>
        </c:when>
        <c:otherwise> <!-- unsaved strategy -->
          <c:set var="makePublicAction" value="$(this).prop('checked',false); wdk.history.showUpdateDialog(this, true, true, true);"/>
        </c:otherwise>
      </c:choose>
      <c:set var="checkedProp" value=""/>
      <c:if test="${strategy.isPublic}">
        <c:set var="checkedProp">checked="checked"</c:set>
      </c:if>
      <td nowrap style="text-align:center;width:5em">
        <imp:image style="display:none" src="/wdk/images/filterLoading.gif"/>
        <input title="${checkboxTitle}" class="isPublicCheckbox" type="checkbox" ${disabledProp} ${checkedProp} onclick="${makePublicAction}"/>
      </td>
    </tr>

    <c:set var="i" value="${i+1}"/>
  </c:forEach>
  <!-- end of forEach strategy in the category -->
  </tbody>
</table>
<!-- end strategyTable.tag -->
