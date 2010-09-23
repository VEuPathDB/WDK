<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>

<%@ attribute name="model"
	      type="org.gusdb.wdk.model.jspwrap.WdkModelBean"
              required="true"
              description="Wdk Model Object for this site"
%>

<%@ attribute name="rcName"
	      required="true"
              description="RecordClass Object for the Answer"
%>

<%@ attribute name="prevStepNum"
	      required="false"
	      description="Step number for transform param urls"
%>

<%@ attribute name="isAdd"
	      required="false"
	      description="true = adding a step.  false = inserting a step"
%>

<c:set var="siteName" value="${applicationScope.wdkModel.name}" />
<c:set var="qSetName" value="none" />
<c:set var="qSets" value="${model.questionSetsMap}" />
<c:set var="qSet" value="${qSets[qSetName]}" />
<c:set var="user" value="${sessionScope.wdkUser}"/>
<%-- the type is of the previous step, that is the input type of the new step  --%>
<c:set var="recordClass" value="${model.recordClassMap[rcName]}" />

<c:if test="${isAdd == 'false'}">
    <%-- insert a step in between, the transform cannot change type in this case --%>
    <jsp:setProperty name="recordClass" property="changeType" value="false" />
</c:if>
<c:set var="transformQuestions" value="${recordClass.transformQuestions}" />





<div id="query_form" style="min-height:140px;">
<span class="dragHandle"><div class="modal_name"><h1 style="font-size:130%;margin-top:4px;" id="query_form_title"></h1></div><a class='close_window' href='javascript:closeAll()'><img src="<c:url value='/wdk/images/Close-X-box.png'/>" alt='Close'/></a></span>

<div id="query_selection">

	<table width="90%">
		<tr><th title="This search will be combined (AND,OR,NOT) with the previous step.">Select a Search</th>
                    <th>-or-</th>

<c:if test="${recordClass.hasBasket}">
                    <th title="Use a copy of your ${recordClass.type} Basket. The effect is as if you run the search -${recordClass.type}s by ID- and provide the IDs in your basket">From Basket</th>
                    <th>-or-</th>
</c:if>

<c:if test="${fn:length(transformQuestions) > 0}">
                    <th title="The transform converts the input set of IDs (from the previous step) into a new set of IDs">Select a Transform</th>
                    <th>-or-</th>
</c:if>


                    <th title="Adding a strategy as a step allows you to generate non-linear strategies (trees).">Select a Strategy</th></tr>
		<tr>
				<td>

<wdk:listCategories category="${rcName}" />

</td>
<td></td>

<c:if test="${recordClass.hasBasket}">
<td>
    <c:set var="q" value="${recordClass.snapshotBasketQuestion}" />
    <ul class="top_nav">
      <li style="width:auto;z-index:40;">
        <a title="Make sure your basket is not empty!" href="javascript:getQueryForm('showQuestion.do?questionFullName=${q.fullName}&partial=true')">${q.displayName}</a>
      </li>
    </ul>
</td>

<td></td>
</c:if>

<c:if test="${fn:length(transformQuestions) > 0}">
<td>
  <ul id="transforms" class="top_nav">
    <c:forEach items="${transformQuestions}" var="t">
      <jsp:setProperty name="t" property="inputType" value="${rcName}" />
      <c:set var="tparams" value="" />
      <c:forEach items="${t.transformParams}" var="tp">
	<c:set var="tparams" value="${tparams}&${tp.name}=${prevStepNum}" />
      </c:forEach>
      <li style="width:auto;z-index:40;"><a href="javascript:getQueryForm('showQuestion.do?questionFullName=${t.fullName}${tparams}&partial=true', true)">${t.displayName}</a></li>
    </c:forEach>
  </ul>
</td>

<td></td>
</c:if>

<td>
	<select id="selected_strategy" type="multiple">
		<option value="--">--Choose a strategy to add--</option>
		<!-- Display the currently ACTIVE (OPENED) Strategies -->
		<option value="--">----Opened strategies----</option>
		<c:forEach items="${user.activeStrategies}" var="storedStrategy">
		 	<c:if test="${storedStrategy.type == rcName}">
				<c:set var="displayName" value="${storedStrategy.name}" />
				<c:if test="${fn:length(displayName) > 30}">
                                    <c:set var="displayName" value="${fn:substring(displayName,0,27)}..." />
                                </c:if>
				<option value="${storedStrategy.strategyId}">&nbsp;&nbsp;${displayName}<c:if test="${!storedStrategy.isSaved}">*</c:if></option>
			</c:if>
		</c:forEach>
		<!-- Display the Saved Strategies -->
		<option value="--">----Saved strategies----</option>
		<c:forEach items="${user.savedStrategiesByCategory[rcName]}" var="storedStrategy">
				<c:set var="displayName" value="${storedStrategy.name}" />
				<c:if test="${fn:length(displayName) > 30}">
                                    <c:set var="displayName" value="${fn:substring(displayName,0,27)}..." />
                                </c:if>
				<option value="${storedStrategy.strategyId}">&nbsp;&nbsp;${displayName}</option>
		</c:forEach>
		<!-- Display the recent Strategies (Opened  viewed in the last 24 hours) -->
		<option value="--">----Recent strategies----${currentTime}</option>
		<c:forEach items="${user.recentStrategiesByCategory[rcName]}" var="storedStrategy">
				<c:set var="displayName" value="${storedStrategy.name}" />
				<c:if test="${fn:length(displayName) > 30}">
                                    <c:set var="displayName" value="${fn:substring(displayName,0,27)}..." />
                                </c:if>
				<option value="${storedStrategy.strategyId}">&nbsp;&nbsp;${displayName}<c:if test="${!storedStrategy.isSaved}">*</c:if></option>
		</c:forEach>
	</select>
	<br><br><input id="continue_button" type="button" value="Continue..."/>
</td>
</tr>
</table>

</div><!-- End of Query Selection Div -->
	<div class="bottom-close"><a class='close_window' href='javascript:closeAll(false)'>Close</a>
</div><!-- End of Query Form Div -->

