<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pg" uri="http://jsptags.com/tags/navigation/pager" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>

<c:set var="wdkAnswer" value="${requestScope.wdkAnswer}"/>
<c:set var="wdkStep" value="${requestScope.wdkStep}"/>
<c:set var="qName" value="${wdkAnswer.question.fullName}" />
<c:set var="modelName" value="${applicationScope.wdkModel.name}" />
<c:set var="recordName" value="${wdkAnswer.question.recordClass.fullName}" />
<c:set var="recHasBasket" value="${wdkAnswer.question.recordClass.hasBasket}" />
<c:set var="dispModelName" value="${applicationScope.wdkModel.displayName}" />
<c:set var="answerRecords" value="${wdkAnswer.records}" />

<%@ attribute name="strategy"
			  type="org.gusdb.wdk.model.jspwrap.StrategyBean"
              required="true"
              description="Strategy Id we are looking at"
%>

<c:set var="type" value="Results" />
<c:set var="step_dataType" value="${wdkStep.displayType}" />
<c:choose>
	<c:when test="${fn:endsWith(step_dataType,'y')}">
		<c:set var="type" value="${fn:substring(step_dataType,0,fn:length(step_dataType)-1)}ies" />
	</c:when>
	<c:otherwise>
		<c:set var="type" value="${step_dataType}s" />
	</c:otherwise>	
</c:choose>

<c:set var="qsp" value="${fn:split(wdk_query_string,'&')}" />
<c:set var="commandUrl" value="" />
<c:forEach items="${qsp}" var="prm">
  <c:if test="${fn:split(prm, '=')[0] eq 'strategy'}">
    <c:set var="commandUrl" value="${commandUrl}${prm}&" />
  </c:if>
  <c:if test="${fn:split(prm, '=')[0] eq 'step'}">
    <c:set var="commandUrl" value="${commandUrl}${prm}&" />
  </c:if>
  <c:if test="${fn:split(prm, '=')[0] eq 'subquery'}">
    <c:set var="commandUrl" value="${commandUrl}${prm}&" />
  </c:if>
  <c:if test="${fn:split(prm, '=')[0] eq 'summary'}">
    <c:set var="commandUrl" value="${commandUrl}${prm}&" />
  </c:if>
</c:forEach>
<c:choose>
  <c:when test="${strategy != null}"> <%-- this is on the run page --%>
    <c:set var="commandUrl" value="${commandUrl}strategy_checksum=${strategy.checksum}" />
  </c:when>
  <c:otherwise> <%-- this is on the basket page --%>
    <c:set var="commandUrl" value="${commandUrl}from_basket=true" />
  </c:otherwise>
</c:choose>
<c:set var="commandUrl"><c:url value="/processSummary.do?${commandUrl}" /></c:set>

<c:if test="${strategy != null}">
    <wdk:filterLayouts strategyId="${strategy.strategyId}" 
                       stepId="${wdkHistory.stepId}"
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
<td class="h4left" style="vertical-align:middle;padding-bottom:7px;">
    <c:if test="${strategy != null}">
        <span id="text_strategy_number">${strategy.name}</span> 
        (step <span id="text_step_number">${strategy.length}</span>) - 
    </c:if>
    ${wdkAnswer.resultSize} <span id="text_data_type">${type}</span>
</td>

<td  style="vertical-align:middle;text-align:right;white-space:nowrap;">
  <div style="float:right">
   <c:if test="${strategy != null}">
    <c:choose>
      <c:when test="${wdkUser.guest}">
        <c:set var="basketClick" value="popLogin()" />
      </c:when>
      <c:otherwise>
        <c:set var="basketClick" value="updateBasket(this, '${wdkStep.stepId}', '0', '${modelName}', '${recordName}')" />
      </c:otherwise>
    </c:choose>
    <c:if test="${recHasBasket}"><a style="font-size:120%" href="javascript:void(0)" onClick="${basketClick}"><b>Add Results to Basket</b></a>&nbsp;|&nbsp;</c:if>
   </c:if>
    <a style="font-size:120%" href="downloadStep.do?step_id=${wdkHistory.stepId}"><b>Download Results</b></a>
  <c:if test="${!empty sessionScope.GALAXY_URL}">
    &nbsp;|&nbsp;<a href="downloadStep.do?step_id=${wdkHistory.stepId}&wdkReportFormat=tabular"><b>SEND TO GALAXY</b></a>
  </c:if>
  </div>
</td>
</tr></table>


<div id='Results_Pane'>
<!-- pager -->
<pg:pager isOffset="true"
          scope="request"
          items="${wdk_paging_total}"
          maxItems="${wdk_paging_total}"
          url="${wdk_paging_url}"
          maxPageItems="${wdk_paging_pageSize}"
          export="currentPageNumber=pageNumber">
  <c:forEach var="paramName" items="${wdk_paging_params}">
    <pg:param name="${paramName}" id="pager" />
  </c:forEach>
  <c:if test="${wdk_summary_checksum != null}">
    <pg:param name="summary" id="pager" />
  </c:if>
  <c:if test="${wdk_sorting_checksum != null}">
    <pg:param name="sort" id="pager" />
  </c:if>

<%--------- PAGING TOP BAR ----------%>
<table width="100%" border="0" cellpadding="3" cellspacing="0">
	<tr class="subheaderrow">
	<th style="text-align: left;white-space:nowrap;"> 
	       <wdk:pager pager_id="top"/> 
	</th>
	<th style="text-align: right;white-space:nowrap;">
		           <%-- display a list of sortable attributes --%>
		           <c:set var="addAttributes" value="${wdkAnswer.displayableAttributes}" />
		           <select id="addAttributes" style="display:none;" commandUrl="${commandUrl}" multiple="multiple">
		               <option value="">--- Add Column ---</option>
		               <c:forEach items="${addAttributes}" var="attribute">
		                 <option value="${attribute.name}" title="${attribute.help}">${attribute.displayName}</option>
		               </c:forEach>
		           </select>
	</th>
	<th style="text-align: right;white-space:nowrap;width:5%;">
	    &nbsp;
	   <input type="button" value="Reset Columns" onClick="resetAttr('${commandUrl}', this)" />
	</th>
	</tr>
</table>
<%--------- END OF PAGING TOP BAR ----------%>

<!-- content of current page -->
<c:set var="sortingAttrNames" value="${wdkAnswer.sortingAttributeNames}" />
<c:set var="sortingAttrOrders" value="${wdkAnswer.sortingAttributeOrders}" />

<%--------- RESULTS  ----------%>
<div id="Results_Div" class="flexigrid">
<div class="bDiv">
<div class="bDivBox">
<table id="Results_Table" width="100%" border="0" cellpadding="3" cellspacing="0" step="${wdkStep.stepId}">
<thead>
<tr class="headerrow">
            <c:if test="${recHasBasket}">
              <th>
              <c:choose>
                <c:when test="${wdkUser.guest}">
                  <c:set var="basketClick" value="popLogin()" />
                  <c:set var="basketTitle" value="Please log in to use the basket." />
                </c:when>
                <c:otherwise>
                  <c:set var="basketClick" value="updateBasket(this,'page', '0', '${modelName}', '${wdkAnswer.recordClass.fullName}')" />
                </c:otherwise>
              </c:choose>
                <a href="javascript:void(0)" onclick="${basketClick}">
		  <img title="${basketTitle}" class="head basket" src="<c:url value='/wdk/images/basket_gray.png'/>" height="16" width="16" value="0"/>
		</a>
              </th>
            </c:if>
  <c:set var="j" value="0"/>
  <c:forEach items="${wdkAnswer.summaryAttributes}" var="sumAttrib">
    <c:set var="attrName" value="${sumAttrib.name}" />
    <th id="${attrName}" align="left" valign="middle">
	<table>
          <tr>
            <td>
		<table>
                  <tr>
                    <td style="padding:0;">
          <c:choose>
            <c:when test="${!sumAttrib.sortable}">
              <img src="<c:url value='/wdk/images/results_arrw_up_blk.png'/>" border="0" alt="Sort up"/>
            </c:when>
            <c:when test="${attrName == sortingAttrNames[0] && sortingAttrOrders[0]}">
              <img src="<c:url value='/wdk/images/results_arrw_up_gr.png'/>"  alt="Sort up" 
                  title="Result is sorted by ${sumAttrib}" />
            </c:when>
            <c:otherwise>
              <%-- display sorting buttons --%>
              <c:choose>
                <c:when test="${strategy != null}">
                  <c:set var="resultsAction" value="javascript:GetResultsPage('${commandUrl}&command=sort&attribute=${attrName}&sortOrder=asc', true, true)" />
                </c:when>
                <c:otherwise>
                  <c:set var="resultsAction" value="javascript:ChangeBasket('${commandUrl}&command=sort&attribute=${attrName}&sortOrder=asc')" />
                </c:otherwise>
              </c:choose>
              <a href="${resultsAction}" title="Sort by ${sumAttrib}">
                  <img src="<c:url value='/wdk/images/results_arrw_up.png'/>" alt="Sort up" border="0" /></a>
            </c:otherwise>
          </c:choose>
                 </td>
               </tr>
               <tr>
                 <td style="padding:0;">
	  <c:choose>
            <c:when test="${!sumAttrib.sortable}">
	      <img src="<c:url value='/wdk/images/results_arrw_dwn_blk.png'/>" border="0" />
	    </c:when>
            <c:when test="${attrName == sortingAttrNames[0] && !sortingAttrOrders[0]}">
              <img src="<c:url value='/wdk/images/results_arrw_dwn_gr.png'/>" alt="Sort down" 
	                    title="Result is sorted by ${sumAttrib}" />
            </c:when>
            <c:otherwise>
              <%-- display sorting buttons --%>
              <c:choose>
                <c:when test="${strategy != null}">
                  <c:set var="resultsAction" value="javascript:GetResultsPage('${commandUrl}&command=sort&attribute=${attrName}&sortOrder=desc', true, true)" />
                </c:when>
                <c:otherwise>
                  <c:set var="resultsAction" value="javascript:ChangeBasket('${commandUrl}&command=sort&attribute=${attrName}&sortOrder=desc')" />
                </c:otherwise>
              </c:choose>
              <a href="${resultsAction}" title="Sort by ${sumAttrib}">
              <img src="<c:url value='/wdk/images/results_arrw_dwn.png'/>" alt="Sort down" border="0" /></a>
            </c:otherwise>
          </c:choose>
                   </td>
                 </tr>
               </table>
             </td>
        <td style="white-space:nowrap;"><span title="${sumAttrib.help}">${sumAttrib.displayName}</span></td>
        <%-- <c:if test="${j != 0}">
          <div style="float:left;">
            <a href="javascript:void(0)">
              <img src="<c:url value='/wdk/images/results_grip.png'/>" alt="" border="0" /></a>
          </div>
        </c:if> --%>
        <c:if test="${j != 0}">
          <td style="width:20px;">
            <%-- display remove attribute button --%>
              <c:choose>
                <c:when test="${strategy != null}">
                  <c:set var="resultsAction" value="javascript:GetResultsPage('${commandUrl}&command=remove&attribute=${attrName}', true, true)" />
                </c:when>
                <c:otherwise>
                  <c:set var="resultsAction" value="javascript:ChangeBasket('${commandUrl}&command=remove&attribute=${attrName}')" />
                </c:otherwise>
              </c:choose>
            <a href="${resultsAction}"
                        title="Remove ${sumAttrib} column">
              <img src="<c:url value='/wdk/images/results_x.png'/>" alt="Remove" border="0" /></a>
          </td>
        </c:if>
         </tr>
      </table>
    </th>
  <c:set var="j" value="${j+1}"/>
  </c:forEach>
</tr>
</thead>
<tbody id="rootBody">

<c:set var="i" value="0"/>

<c:forEach items="${answerRecords}" var="record">
    <c:set value="${record.primaryKey}" var="primaryKey"/>
	<c:set var="recNam" value="${record.recordClass.fullName}"/>
<c:choose>
  <c:when test="${i % 2 == 0}"><tr class="lines"></c:when>
  <c:otherwise><tr class="linesalt"></c:otherwise>
</c:choose>



	<c:if test="${recHasBasket}">
          <td>
		<c:set value="${record.attributes['in_basket']}" var="is_basket"/>
		<c:set var="basket_img" value="basket_gray.png"/>
		<c:if test="${is_basket == '1'}">
			<c:set var="basket_img" value="basket_color.png"/>
		</c:if>
            <c:if test="${!wdkUser.guest}">
              <c:set var="basketClick" value="updateBasket(this,'single', '${primaryKey.value}', '${modelName}', '${recNam}')" />
            </c:if>
	    <a href="javascript:void(0)" onclick="${basketClick}">
	      <img title="${basketTitle}" class="basket" value="${is_basket}" src="<c:url value='wdk/images/${basket_img}'/>" width="16" height="16"/>
	    </a>
            </td>
          </c:if>

  <c:set var="j" value="0"/>

  <c:forEach items="${wdkAnswer.summaryAttributeNames}" var="sumAttrName">
    <c:set value="${record.summaryAttributes[sumAttrName]}" var="recAttr"/>
    <c:set var="align" value="align='${recAttr.attributeField.align}'" />
    <c:set var="nowrap">
        <c:if test="${recAttr.attributeField.nowrap}">white-space:nowrap;</c:if>
    </c:set>


    <c:set var="pkValues" value="${primaryKey.values}" />
    <c:set var="recordLinkKeys" value="" />
    <c:forEach items="${pkValues}" var="pkValue">
      <c:set var="recordLinkKeys" value="${recordLinkKeys}&${pkValue.key}=${pkValue.value}" />
    </c:forEach>

    <td ${align} style="${nowrap}padding:3px 2px"><div>
      
      <c:set var="fieldVal" value="${recAttr.briefDisplay}"/>
      <!-- store the primary key pairs here -->
      <div class="primaryKey" style="display:none;">
        <c:forEach items="${pkValues}" var="pkValue">
          <span key="${pkValue.key}">${pkValue.value}</span>
        </c:forEach>
      </div>
      <c:choose>
        <c:when test="${j == 0}">
	  <%-- display a link to record page --%>
          <a href="showRecord.do?name=${recNam}${recordLinkKeys}">${fieldVal}</a>
        </c:when>   <%-- when j=0 --%>

        <c:otherwise>

          <!-- need to know if fieldVal should be hot linked -->
          <c:choose>
			<c:when test="${fieldVal == null || fn:length(fieldVal) == 0}">
               <span style="color:gray;">N/A</span>
            </c:when>
            <c:when test="${recAttr.class.name eq 'org.gusdb.wdk.model.LinkAttributeValue'}">
		 <a href="${recAttr.url}">${recAttr.displayText}</a>
            </c:when>
            <c:otherwise>
              ${fieldVal}
            </c:otherwise>
          </c:choose>

        </c:otherwise>
      </c:choose>
    </div></td>
    <c:set var="j" value="${j+1}"/>

  </c:forEach>
</tr>
<c:set var="i" value="${i+1}"/>
</c:forEach>

</tr>

</tbody>
</table>
</div>
</div>
</div>
<%--------- END OF RESULTS  ----------%>

<%--------- PAGING BOTTOM BAR ----------%>
<table width="100%" border="0" cellpadding="3" cellspacing="0">
	<tr class="subheaderrow">
	<th style="text-align:left;white-space:nowrap;"> 
	       <wdk:pager pager_id="bottom"/> 
	</th>
	<th style="text-align:right;white-space:nowrap;">
		&nbsp;
	</th>
	<th style="text-align:right;white-space:nowrap;width:5%;">
	    &nbsp;
	</th>
	</tr>
</table>
<%--------- END OF PAGING BOTTOM BAR ----------%>
</pg:pager>
</div> <!-- END OF RESULTS PANE -->

  </c:otherwise>
</c:choose>
