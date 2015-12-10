<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pg" uri="http://jsptags.com/tags/navigation/pager" %>
<%@ taglib prefix="imp" tagdir="/WEB-INF/tags/imp" %>

<%@ attribute name="step"
              type="org.gusdb.wdk.model.jspwrap.StepBean"
              required="true"
              description="Step bean we are looking at" %>
<%@ attribute name="showNativeCount"
              type="java.lang.String"
              required="false"
              description="if true, we show the native count" %>
<%@ attribute name="missingNative"
              type="java.lang.String"
              required="false"
              description="if true, we have missing native ids in result" %>

  <c:set var="wdkAnswer" value="${step.answerValue}"/>
  <c:set var="wdkViewAnswer" value="${step.viewAnswerValue}"/>

  <c:set var="qName" value="${wdkAnswer.question.fullName}" />
  <c:set var="modelName" value="${applicationScope.wdkModel.name}" />
  <c:set var="recordName" value="${wdkAnswer.question.recordClass.fullName}" />
  <c:set var="recHasBasket" value="${wdkAnswer.question.recordClass.useBasket}" />
  <c:set var="dispModelName" value="${applicationScope.wdkModel.displayName}" />

 <c:set var="displayNamePlural" value="${wdkAnswer.question.recordClass.displayNamePlural}" />
 <c:set var="nativeDisplayNamePlural" value="${wdkAnswer.question.recordClass.nativeDisplayNamePlural}" />

  <%-- catch raised exception so we can show the user a nice message --%>
  <c:catch var="answerValueRecords_exception">
    <%-- FIXME This should probably be logged to wdk logger --%>
    <c:set var="answerRecords" value="${wdkViewAnswer.records}" />
  </c:catch>

  <c:set var="wdkView" value="${requestScope.wdkView}" />
  <c:set var="displayName" value="${step.recordClass.displayName}"/>
  <c:set var="isBasket" value="${fn:contains(step.questionName, 'ByRealtimeBasket')}"/>

  <c:choose>

    <%-- Handle exception raised when accessing answerValue, when we're viewing a basket --%>
    <c:when test='${answerValueRecords_exception ne null and isBasket}'>
      <div class="ui-widget">
        <div class="ui-state-error ui-corner-all" style="padding:8px;">
          <p>
            <span class="ui-icon ui-icon-alert" style="float: left; margin-right: .3em;"></span>
            <div><imp:verbiage key="answer-value-records-error-msg.basket.content"/></div>
          </p>
        </div>
      </div>
    </c:when>

    <%-- Handle exception raised when accessing answerValue, when we're viewing a step result --%>
    <c:when test='${answerValueRecords_exception ne null}'>
      <div class="ui-widget">
        <div class="ui-state-error ui-corner-all" style="padding:8px;">
          <p>
            <span class="ui-icon ui-icon-alert" style="float: left; margin-right: .3em;"></span>
              <div><imp:verbiage key="answer-value-records-error-msg.default.content"/></div>
          </p>
        </div>
      </div>
    </c:when>

    <c:when test='${wdkViewAnswer.resultSize == 0}'>
      No results are retrieved
    </c:when>

    <c:otherwise>

      <%-- pager --%>
      <pg:pager isOffset="true"
                scope="request"
                items="${wdk_paging_total}"
                maxItems="${wdk_paging_total}"
                url="${requestUri}"
                maxPageItems="${wdk_paging_pageSize}"
                export="offset,currentPageNumber=pageNumber">
        <c:forEach var="paramName" items="${wdk_paging_params}">
          <pg:param name="${paramName}" id="pager" />
        </c:forEach>
        <c:if test="${wdk_summary_checksum != null}">
          <pg:param name="summary" id="pager" />
        </c:if>
        <c:if test="${wdk_sorting_checksum != null}">
          <pg:param name="sort" id="pager" />
        </c:if>

 <%--  debuging line:       <pg:page> ${pageNumber} </pg:page> --%>

        <%--------- PAGING TOP BAR ----------%>
        <c:url var="commandUrl" value="/processSummaryView.do?step=${step.stepId}&view=${wdkView.name}&pager.offset=${offset}" />
        <table  width="100%">
          <tr class="subheaderrow">

            <th style="text-align: left; white-space: nowrap; width: 33%;"> 
              <imp:pager wdkAnswer="${wdkViewAnswer}" pager_id="top"/> 
            </th>

            <th style="text-align: center; white-space: nowrap; width: 34px;">

<%-- <c:if test="${showNativeCount eq 'true'}">   --%>
              <c:if test="${wdkAnswer.question.recordClass.hasResultSizeQuery}">
                <span style="padding-right: 2em">
                  ${wdkAnswer.displayResultSize eq 1 ? step.recordClass.displayName : step.recordClass.displayNamePlural}:
                  ${wdkAnswer.displayResultSize}
                </span>
              </c:if>
              <span style="padding-right: 2em" title="${trTitle}">
                ${wdkAnswer.resultSize eq 1 ? wdkAnswer.question.recordClass.nativeDisplayName : wdkAnswer.question.recordClass.nativeDisplayNamePlural}:
                ${wdkAnswer.resultSize}
              </span>
<%--  </c:if> --%>
            </th>

            <th style="text-align: right; white-space: nowrap; width: 33px;">
              <c:if test="${wdkViewAnswer.resultSize > 0}">

                <%-- Galaxy URL --%>
                <c:if test="${!empty sessionScope.GALAXY_URL}">
                  <a href="downloadStep.do?step_id=${step.stepId}&wdkReportFormat=tabular">
                    <b class="galaxy">SEND TO GALAXY</b>
                  </a>
                </c:if>

                <c:choose>
                  <c:when test="${wdkUser.guest}">
                    <c:set var="basketClick" value="wdk.user.login();" />
                  </c:when>
                  <c:otherwise>
                    <c:set var="basketClick" value="wdk.basket.updateBasket(this, '${step.stepId}', '0', '0', '${recordName}');" /> <!-- fourth param is unused (basket.js) -->
                  </c:otherwise>
                </c:choose>

                <a style="padding-right: 1em;" href="downloadStep.do?step_id=${step.stepId}&signature=${wdkUser.signature}">
                  <b>Download</b>
                </a>

                <c:if test="${recHasBasket}">
                  <a style="padding-right: 1em;" id="basketStep" href="javascript:void(0)" onClick="${basketClick}">
                    <b>Add to Basket</b>
                  </a>
                </c:if>
              </c:if>
              <imp:addAttributes wdkAnswer="${wdkViewAnswer}" commandUrl="${commandUrl}"/>
            </th>
          </tr>
        </table>
        <%--------- END OF PAGING TOP BAR ----------%>

        <%-- content of current page --%>
        <c:set var="sortingAttrNames" value="${wdkViewAnswer.sortingAttributeNames}" />
        <c:set var="sortingAttrOrders" value="${wdkViewAnswer.sortingAttributeOrders}" />

        <%--------- RESULTS  ----------%>
       <div class="result-table-data" data-commandurl="${commandUrl}"></div>
       <div class="Results_Div flexigrid">
          <div class="bDiv">
            <div class="bDivBox">

              <table  style="width:100%" class="Results_Table" step="${step.stepId}">

<%-- TABLE HEADER ROW --%>
                <thead>
                  <tr class="headerrow">
                    <c:if test="${recHasBasket}">  
<%--------- BASKET COLUMN  ----------%>
                      <th>
                        <c:choose>
                          <c:when test="${wdkUser.guest}">
                            <c:set var="basketClick" value="wdk.user.login();" />
                            <c:set var="basketTitle" value="Please log in to use the basket." />
                          </c:when>
                          <c:otherwise>
                            <c:set var="basketClick" value="wdk.basket.updateBasket(this,'page', '0', '${modelName}', '${wdkAnswer.recordClass.fullName}')" />
                          </c:otherwise>
                        </c:choose>
                        <a id="basketPage" href="javascript:void(0)" onclick="${basketClick}">
                          <imp:image title="${basketTitle}" class="head basket" src="wdk/images/basket_gray.png" height="16" width="16" value="0"/>
                        </a>
                      </th>
                    </c:if>
                    <c:set var="j" value="0"/>
                    <c:forEach items="${wdkViewAnswer.summaryAttributes}" var="sumAttrib">   

<%--------- OTHER COLUMNS  ----------%>
                    <%--------- SHOW Prim Key COLUMN (j=0) ONLY IF DISPLAYNAME is non empty (in model.xml) ----------%>
                    <c:if test="${not empty sumAttrib.displayName || j != 0}">    
                      <%-- FLAG for second loop when showing column values --%>   
                      <c:if test="${j == 0}"><c:set var="showPrimKey" value="yes"/></c:if>  

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
                                        <%-- img src="wdk/images/results_arrw_up_blk.png" border="0" alt="Sort up"/ --%>
                                      </c:when>
                                      <c:when test="${attrName eq sortingAttrNames[0] and sortingAttrOrders[0]}">
                                        <imp:image src="wdk/images/results_arrw_up_gr.png"  alt="Sort up" title="Result is sorted by ${sumAttrib}" />
                                      </c:when>
                                      <c:otherwise>
                                        <%-- display sorting buttons --%>
                                        <c:set var="resultsAction" value="javascript:wdk.resultsPage.sortResult('${attrName}', 'asc')" />
                                        <a href="${resultsAction}" title="Sort by ${sumAttrib}">
                                          <imp:image src="wdk/images/results_arrw_up.png" alt="Sort up" border="0" />
                                        </a>
                                      </c:otherwise>
                                    </c:choose>
                                  </td>
                                </tr>
                                <tr>
                                  <td style="padding:0;">
                                    <c:choose>
                                      <c:when test="${!sumAttrib.sortable}">
                                        <%-- img src="wdk/images/results_arrw_dwn_blk.png" border="0" / --%>
                                      </c:when>
                                      <c:when test="${attrName eq sortingAttrNames[0] and not sortingAttrOrders[0]}">
                                        <imp:image src="wdk/images/results_arrw_dwn_gr.png" alt="Sort down" title="Result is sorted by ${sumAttrib}" />
                                      </c:when>
                                      <c:otherwise>
                                        <%-- display sorting buttons --%>
                                        <c:set var="resultsAction" value="javascript:wdk.resultsPage.sortResult('${attrName}', 'desc')" />
                                        <a href="${resultsAction}" title="Sort by ${sumAttrib}">
                                          <imp:image src="wdk/images/results_arrw_dwn.png" alt="Sort down" border="0" />
                                        </a>
                                      </c:otherwise>
                                    </c:choose>
                                  </td>
                                </tr>
                              </table>
                            </td>
                            <%-- <td style="white-space:nowrap;"><span title="${sumAttrib.help}">${sumAttrib.displayName}</span></td> --%>
                            <td>
                              <span title="${sumAttrib.help}">${sumAttrib.displayName}</span>
                            </td>
                            <%-- <c:if test="${j != 0}">
                              <div style="float:left;">
                                <a href="javascript:void(0)">
                                  <imp:image src="wdk/images/results_grip.png" alt="" border="0" /></a>
                              </div>
                            </c:if> --%>
                            <c:if test="${sumAttrib.removable}">
                              <td style="width:20px;">
                                <%-- display remove attribute button --%>
                                <c:set var="resultsAction" value="javascript:wdk.resultsPage.removeAttribute('${attrName}')" />
                                <a href="${resultsAction}" title="Remove ${sumAttrib} column">
                                  <imp:image src="wdk/images/results_x.png" alt="Remove" border="0" />
                                </a>
                              </td>
                            </c:if>
                            <td>
                              <imp:attributePlugin attribute="${sumAttrib}" />
                            </td>
                          </tr>
                        </table>
                      </th>

                    </c:if>
                    <c:set var="j" value="${j+1}"/>
                    </c:forEach>
                  </tr>
                </thead>

<%-- TABLE RESULT ROWS --%>
                <tbody class="rootBody">
                  <c:set var="i" value="0"/>

<%-- FOR EACH ROW --%>
                  <c:forEach items="${answerRecords}" var="record">
                    <c:set value="${record.primaryKey}" var="primaryKey"/>
                    <c:set var="recNam" value="${record.recordClass.fullName}"/>
                    <tr class="${i % 2 eq 0 ? 'lines' : 'linesalt'}">
<%--------- BASKET COLUMN  ----------%>
                      <c:if test="${recHasBasket}">            
                        <td>
                          <c:set var="basket_img" value="basket_gray.png"/>
                          <c:set var="basketId" value="basket${fn:replace(primaryKey.value,'.','_')}" />
                          <c:choose>
                            <c:when test="${!wdkUser.guest}">
                              <c:set value="${record.attributes['in_basket']}" var="is_basket"/>
                              <c:set var="basketTitle" value="Click to add this item to the basket." />
                              <c:if test="${is_basket == '1'}">
                                <c:set var="basket_img" value="basket_color.png"/>
                                <c:set var="basketTitle" value="Click to remove this item from the basket." />
                              </c:if>
                              <c:set var="basketClick" value="wdk.basket.updateBasket(this,'single', '${primaryKey.value}', '${modelName}', '${recNam}')" />
                            </c:when>
                            <c:otherwise>
                              <c:set var="basketClick" value="wdk.user.login();" />
                              <c:set var="basketTitle" value="Please log in to use the basket." />
                            </c:otherwise>
                          </c:choose>
                          <a id="${basketId}" href="javascript:void(0)" onclick="${basketClick}">
                            <imp:image title="${basketTitle}" class="basket" value="${is_basket}" src="wdk/images/${basket_img}" width="16" height="16"/>
                          </a>
                        </td>
                      </c:if>

<%------ FOR EACH OTHER COLUMN IN ROW --------%>
                      <c:set var="j" value="0"/>
                      <c:forEach items="${wdkViewAnswer.summaryAttributeNames}" var="sumAttrName">    
                        <%--------- SHOW Prim Key COLUMN IF showPrimKey defined  ----------%>
                        <c:if test="${not empty showPrimKey ||  j != 0}"> 
                          <c:set value="${record.summaryAttributes[sumAttrName]}" var="recAttr"/>
                          <imp:wdkAttribute attributeValue="${recAttr}" truncate="true" recordName="${recNam}" />
                        </c:if>    
                        <c:set var="j" value="${j+1}"/>
                      </c:forEach>
                    </tr>
                    <c:set var="i" value="${i+1}"/>
                  </c:forEach>
                </tbody>
              </table>

            </div>
          </div>
        </div>

        <%--------- END OF RESULTS  ----------%>



        <%--------- PAGING BOTTOM BAR ----------%>
        <table style="width:100%">
          <tr class="subheaderrow">
            <th style="text-align:left;white-space:nowrap;"> 
              <imp:pager wdkAnswer="${wdkViewAnswer}" pager_id="bottom"/> 
            </th>
          </tr>
        </table>
        <%--------- END OF PAGING BOTTOM BAR ----------%>
      </pg:pager>
    </c:otherwise> <%-- end of resultSize != 0 --%>
  </c:choose>
