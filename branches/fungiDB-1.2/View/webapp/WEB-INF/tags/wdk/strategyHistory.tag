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

<!-- to make appear on a popup probably -->
<div style="text-align:center;font-size:95%;font-style:italic;margin-bottom:7px;border:1px outset darkred;padding:3px;color:darkred">
<b>Important note on new releases:</b> About every 2 months we release new data or annotation (latest release ${releaseDate_formatted}.)
<br>The IDs in <b>any step result might change</b> after a release.
</div>


  <c:set var="typeC" value="0"/>
  <!-- begin creating tabs for history sections -->
  <ul class="menubar">

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
      <c:set var="recDispName" value="${strat.displayType}"/>
      <c:set var="recTabName" value="${fn:replace(recDispName, ' ', '_')}"/>

      <c:set var="typeC" value="${typeC+1}"/>
      <c:if test="${typeC != 1}">
        <li>|</li>
      </c:if>
      <li>
        <a id="tab_${recTabName}" onclick="displayHist('${recTabName}')"
           href="javascript:void(0)">${recDispName}&nbsp;Strategies&nbsp;(${totalStratsCount})</a>
      </li>
    </c:if>
  </c:forEach>

  <c:if test="${fn:length(invalidStrategies) > 0}">
    <li>
      <a id="tab_invalid" onclick="displayHist('invalid')"
       href="javascript:void(0)">Invalid&nbsp;Strategies</a></li>
  </c:if>
  <%-- <li class="cmplt_hist_link">
    <a id="tab_cmplt" onclick="displayHist('cmplt')" href="javascript:void(0)">All My Queries</a>
  </li> --%>
  </ul>

<table class="history_controls clear" width="100%">
   <tr>
      <td width="30%">Select:&nbsp;<a class="check_toggle" onclick="selectAllHist()" href="javascript:void(0)">All</a>&nbsp|&nbsp;
                  <a class="check_toggle" onclick="selectAllHist('saved')" href="javascript:void(0)">Saved</a>&nbsp|&nbsp;
                  <a class="check_toggle" onclick="selectAllHist('unsaved')" href="javascript:void(0)">Unsaved</a>&nbsp|&nbsp;
                  <a class="check_toggle" onclick="selectNoneHist()" href="javascript:void(0)">None</a></td>
      <td width="20%" class="medium">
         <input type="button" value="Open" onclick="handleBulkStrategies('open')"/>
         <input type="button" value="Close" onclick="handleBulkStrategies('close')"/>
         <input type="button" value="Delete" onclick="handleBulkStrategies('delete')"/>
      </td>
<td width="50%" style="text-align:right">
<div title="Also upon a new release, a step will become outdated if we modify the search form, by changing a parameter or its possible values; you need to revise the step!"
	id="invalid-legend"><img src="<c:url value="wdk/images/invalidIcon.png"/>" width="12"/> = strategy contains outdated steps, open to revise (<a style="" href="<c:url value="/wdk/jsp/whyInvalid.jsp"/>" target="_blank" onClick="poptastic(this.href,'unused',250,800); return false;">why?</a>)</div>
</td>

   </tr>
</table>



<!-- begin creating history sections to display strategies -->
<c:forEach items="${unsavedStrategiesMap}" var="strategyEntry">
  <c:set var="type" value="${strategyEntry.key}"/>
  <c:set var="strategies" value="${strategyEntry.value}"/>
  <c:set var="recDispName" value="${strategies[0].displayType}"/>
  <c:set var="recTabName" value="${fn:replace(recDispName, ' ', '_')}"/>

  <c:if test="${fn:length(strategies) > 0}">
    <div class="panel_${recTabName} history_panel unsaved-strategies">
      <imp:strategyTable strategies="${strategies}" wdkUser="${wdkUser}" prefix="Unsaved" />
    </div>
  </c:if>
</c:forEach>
<!-- end of showing strategies grouped by RecordTypes -->
<br/>
<!-- begin creating history sections to display strategies -->
<c:forEach items="${savedStrategiesMap}" var="strategyEntry">
  <c:set var="type" value="${strategyEntry.key}"/>
  <c:set var="strategies" value="${strategyEntry.value}"/>
  <c:set var="recDispName" value="${strategies[0].displayType}"/>
  <c:set var="recTabName" value="${fn:replace(recDispName, ' ', '_')}"/>

  <c:if test="${fn:length(strategies) > 0}">
    <div class="panel_${recTabName} history_panel saved-strategies">
      <imp:strategyTable strategies="${strategies}" wdkUser="${wdkUser}" prefix="Saved" />
    </div>
  </c:if>
</c:forEach>
<!-- end of showing strategies grouped by RecordTypes -->

<c:set var="scheme" value="${pageContext.request.scheme}" />
<c:set var="serverName" value="${pageContext.request.serverName}" />
<c:set var="request_uri" value="${requestScope['javax.servlet.forward.request_uri']}" />
<c:set var="request_uri" value="${fn:substringAfter(request_uri, '/')}" />
<c:set var="request_uri" value="${fn:substringBefore(request_uri, '/')}" />
<c:set var="exportBaseUrl" value = "${scheme}://${serverName}/${request_uri}/im.do?s=" />

<!-- popups for save/rename/share forms -->
<!-- (though share does not need a form, just an input box with the url inside) -->

    <div class='modal_div save_strat' id="hist_save_rename">
      <div class='dragHandle'>
        <div class="modal_name">
          <span id='popup-title' class='h3left'></span>
        </div>
        <a class='close_window' href='javascript:closeModal()'>
        <img alt='Close' src="<c:url value='/wdk/images/Close-X.png'/>"  height='16'/>
        </a>
      </div>
      <form id='save_strat_form' onsubmit='return validateSaveForm(this);'>
        <input type='hidden' value="" name='strategy'/>
        <input type='text' value="" name='name' maxlength='200'/>
        <input  style='margin-left:5px;' type='submit' value='Save'/>
      </form>
    </div>


<%-- invalid strategies, if any --%>
<c:if test="${fn:length(invalidStrategies) > 0}">
    <div class="panel_invalid history_panel unsaved-strategies">
      <imp:strategyTable strategies="${user.invalidStrategies}" wdkUser="${wdkUser}" prefix="Invalid" />
    </div>
</c:if>

<div class="panel_cmplt history_panel">
  <h1>All My Queries</h1>
  <div class="loading"></div>
</div>

<table class="history_controls">
   <tr>
      <td>Select:&nbsp;<a class="check_toggle" onclick="selectAllHist()" href="javascript:void(0)">All</a>&nbsp|&nbsp;
                  <a class="check_toggle" onclick="selectAllHist('saved')" href="javascript:void(0)">Saved</a>&nbsp|&nbsp;
                  <a class="check_toggle" onclick="selectAllHist('unsaved')" href="javascript:void(0)">Unsaved</a>&nbsp|&nbsp;
                  <a class="check_toggle" onclick="selectNoneHist()" href="javascript:void(0)">None</a></td>
      <td class="medium">
         <input type="button" value="Open" onclick="handleBulkStrategies('open')"/>
         <input type="button" value="Close" onclick="handleBulkStrategies('close')"/>
         <input type="button" value="Delete" onclick="handleBulkStrategies('delete')"/>
      </td>
   </tr>
</table>

  </c:otherwise>
</c:choose> 
<!-- end of deciding strategy emptiness -->
<!--end strategyHistory.tag-->



<!-- save_warning is defined in view-JSON.js, where the popups for save/rename/shared in Opened tab, use it -->
<!--  history.js prepares the form defined here, used for these popups in the All tab (history view) -->
<!-- the form is shared by all strategies, we need to add the message only once -->
<script type="text/javascript">
	var myform = $("form#save_strat_form");
	myform.prepend(save_warning);
	$("i,form#save_strat_form").css("font-size","95%");
</script>
