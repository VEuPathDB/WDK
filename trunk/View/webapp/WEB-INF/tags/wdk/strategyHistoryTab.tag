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

<%@ attribute name="recordType"
              required="false"
              description="used to filter strategies"
%>

<!--strategyHistory.tag-->
<c:set var="unsavedStrategiesMap" value="${user.unsavedStrategiesByCategory}"/>
<c:set var="savedStrategiesMap" value="${user.savedStrategiesByCategory}"/>
<c:set var="invalidStrategies" value="${user.invalidStrategies}"/>
<c:set var="modelName" value="${model.name}"/>

<table class="history_controls clear" width="100%">
  <tr>
    <td style="vertical-align:middle" width="30%">
      Select:&nbsp;
      <a class="check_toggle" onclick="wdk.history.selectAllHist()" href="javascript:void(0)">All</a>&nbsp|&nbsp;
      <a class="check_toggle" onclick="wdk.history.selectAllHist('saved')" href="javascript:void(0)">Saved</a>&nbsp|&nbsp;
      <a class="check_toggle" onclick="wdk.history.selectAllHist('unsaved')" href="javascript:void(0)">Unsaved</a>&nbsp|&nbsp;
      <a class="check_toggle" onclick="wdk.history.selectNoneHist()" href="javascript:void(0)">None</a>
    </td>
    <td style="vertical-align:middle" width="20%" class="medium">
      <input type="button" value="Open" onclick="wdk.history.handleBulkStrategies('open')"/>
      <input type="button" value="Close" onclick="wdk.history.handleBulkStrategies('close')"/>
      <input type="button" value="Delete" onclick="wdk.history.handleBulkStrategies('delete')"/>
    </td>
 
    <td width="50%" style="text-align:right">
      <div class="strat-legend"><imp:verbiage key="note.strat-legend.content"/></div>
    </td>
  </tr>
</table>

<%-- unsaved --%>
<c:set var="unsavedStrategies" value="${unsavedStrategiesMap[recordType]}"/>
<c:set var="recDispName" value="${unsavedStrategies[0].recordClass.displayNamePlural}"/>
<c:set var="recTabName" value="${fn:replace(recDispName, ' ', '_')}"/>

<c:if test="${fn:length(unsavedStrategies) > 0}">
  <div class="panel_${recTabName} history_panel unsaved-strategies">
    <imp:strategyTable strategies="${unsavedStrategies}" wdkUser="${wdkUser}" prefix="Unsaved" />
  </div>
</c:if>

<br/>

<%-- saved --%>
<c:set var="savedStrategies" value="${savedStrategiesMap[recordType]}"/>
<c:set var="recDispName" value="${savedStrategies[0].recordClass.displayNamePlural}"/>
<c:set var="recTabName" value="${fn:replace(recDispName, ' ', '_')}"/>

<c:if test="${fn:length(savedStrategies) > 0}">
  <div class="panel_${recTabName} history_panel saved-strategies">
    <imp:strategyTable strategies="${savedStrategies}" wdkUser="${wdkUser}" prefix="Saved" />
  </div>
</c:if>


<%-- invalid strategies, if any --%>
<c:if test="${fn:length(invalidStrategies) > 0}">
  <div class="panel_invalid history_panel unsaved-strategies">
    <imp:strategyTable strategies="${user.invalidStrategies}" wdkUser="${wdkUser}" prefix="Invalid" />
  </div>
</c:if>


<table class="history_controls clear" width="100%">
  <tr>
    <td style="vertical-align:middle" width="30%">
      Select:&nbsp;
      <a class="check_toggle" onclick="wdk.history.selectAllHist()" href="javascript:void(0)">All</a>&nbsp|&nbsp;
      <a class="check_toggle" onclick="wdk.history.selectAllHist('saved')" href="javascript:void(0)">Saved</a>&nbsp|&nbsp;
      <a class="check_toggle" onclick="wdk.history.selectAllHist('unsaved')" href="javascript:void(0)">Unsaved</a>&nbsp|&nbsp;
      <a class="check_toggle" onclick="wdk.history.selectNoneHist()" href="javascript:void(0)">None</a></td>
      <td style="vertical-align:middle" width="20%" class="medium">
        <input type="button" value="Open" onclick="wdk.history.handleBulkStrategies('open')"/>
        <input type="button" value="Close" onclick="wdk.history.handleBulkStrategies('close')"/>
        <input type="button" value="Delete" onclick="wdk.history.handleBulkStrategies('delete')"/>
      </td>
      <td width="50%" style="text-align:right"></td>
   </tr>
</table>
