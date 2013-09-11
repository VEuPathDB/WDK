<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pg" uri="http://jsptags.com/tags/navigation/pager" %>
<%@ taglib prefix="imp" tagdir="/WEB-INF/tags/imp" %>

<%@ attribute name="step"
              type="org.gusdb.wdk.model.jspwrap.StepBean"
              required="true"
              description="Step bean we are looking at" %>


  <c:set var="wdkAnswer" value="${step.answerValue}"/>

  <c:set var="qName" value="${wdkAnswer.question.fullName}" />
  <c:set var="modelName" value="${applicationScope.wdkModel.name}" />
  <c:set var="recordName" value="${wdkAnswer.question.recordClass.fullName}" />
  <c:set var="recHasBasket" value="${wdkAnswer.question.recordClass.useBasket}" />
  <c:set var="dispModelName" value="${applicationScope.wdkModel.displayName}" />

  <c:catch var="answerValueRecords_exception">
    <c:set var="answerRecords" value="${wdkAnswer.records}" />
  </c:catch>

  <c:set var="wdkView" value="${requestScope.wdkView}" />

  <c:set var="displayName" value="${step.recordClass.displayName}"/>

  <c:set var="isBasket" value="${fn:contains(step.questionName, 'ByRealtimeBasket')}"/>

  <c:choose>
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
    <c:when test='${wdkAnswer.resultSize == 0}'>
      No results are retrieved
    </c:when>
    <c:otherwise>

      <%-- pager --%>
      <pg:pager isOffset="true"
                scope="request"
                items="${wdk_paging_total}"
                maxItems="${wdk_paging_total}"
                url="${wdk_paging_url}"
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
            <th style="text-align: left;white-space:nowrap;"> 
              <imp:pager wdkAnswer="${wdkAnswer}" pager_id="top"/> 
            </th>
            <th style="text-align: right;white-space:nowrap;">
              <imp:addAttributes wdkAnswer="${wdkAnswer}" commandUrl="${commandUrl}"/>
            </th>
          </tr>
        </table>
        <%--------- END OF PAGING TOP BAR ----------%>

        <%-- content of current page --%>
        <c:set var="sortingAttrNames" value="${wdkAnswer.sortingAttributeNames}" />
        <c:set var="sortingAttrOrders" value="${wdkAnswer.sortingAttributeOrders}" />

        <%--------- RESULTS  ----------%>

       <div class="Results_Div flexigrid">
          <div class="bDiv">
            <div class="bDivBox">

              <table  style="width:100%" class="Results_Table" step="${step.stepId}">
                <thead>
                  <tr class="headerrow">
                    <c:if test="${recHasBasket}">
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
                                        <%-- img src="<c:url value='/wdk/images/results_arrw_up_blk.png'/>" border="0" alt="Sort up"/ --%>
                                      </c:when>
                                      <c:when test="${attrName eq sortingAttrNames[0] and sortingAttrOrders[0]}">
                                        <img src="<c:url value='/wdk/images/results_arrw_up_gr.png'/>"  alt="Sort up" title="Result is sorted by ${sumAttrib}" />
                                      </c:when>
                                      <c:otherwise>
                                        <%-- display sorting buttons --%>
                                        <c:set var="resultsAction" value="javascript:wdk.resultsPage.sortResult('${attrName}', 'asc')" />
                                        <a href="${resultsAction}" title="Sort by ${sumAttrib}">
                                          <img src="<c:url value='/wdk/images/results_arrw_up.png'/>" alt="Sort up" border="0" />
                                        </a>
                                      </c:otherwise>
                                    </c:choose>
                                  </td>
                                </tr>
                                <tr>
                                  <td style="padding:0;">
                                    <c:choose>
                                      <c:when test="${!sumAttrib.sortable}">
                                        <%-- img src="<c:url value='/wdk/images/results_arrw_dwn_blk.png'/>" border="0" / --%>
                                      </c:when>
                                      <c:when test="${attrName eq sortingAttrNames[0] and not sortingAttrOrders[0]}">
                                        <img src="<c:url value='/wdk/images/results_arrw_dwn_gr.png'/>" alt="Sort down" title="Result is sorted by ${sumAttrib}" />
                                      </c:when>
                                      <c:otherwise>
                                        <%-- display sorting buttons --%>
                                        <c:set var="resultsAction" value="javascript:wdk.resultsPage.sortResult('${attrName}', 'desc')" />
                                        <a href="${resultsAction}" title="Sort by ${sumAttrib}">
                                          <img src="<c:url value='/wdk/images/results_arrw_dwn.png'/>" alt="Sort down" border="0" />
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
                                  <img src="<c:url value='/wdk/images/results_grip.png'/>" alt="" border="0" /></a>
                              </div>
                            </c:if> --%>
                            <c:if test="${sumAttrib.removable}">
                              <td style="width:20px;">
                                <%-- display remove attribute button --%>
                                <c:set var="resultsAction" value="javascript:wdk.resultsPage.removeAttribute('${attrName}')" />
                                <a href="${resultsAction}" title="Remove ${sumAttrib} column">
                                  <img src="<c:url value='/wdk/images/results_x.png'/>" alt="Remove" border="0" />
                                </a>
                              </td>
                            </c:if>
                            <td>
                              <imp:attributePlugin attribute="${sumAttrib}" />
                            </td>
                          </tr>
                        </table>
                      </th>
                      <c:set var="j" value="${j+1}"/>
                    </c:forEach>
                  </tr>
                </thead>
                <tbody class="rootBody">
                  <c:set var="i" value="0"/>

<%-- FOR EACH ROW --%>
                  <c:forEach items="${answerRecords}" var="record">
                    <c:set value="${record.primaryKey}" var="primaryKey"/>
                    <c:set var="recNam" value="${record.recordClass.fullName}"/>
                    <tr class="${i % 2 eq 0 ? 'lines' : 'linesalt'}">
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
                            <img title="${basketTitle}" class="basket" value="${is_basket}" src="<c:url value='wdk/images/${basket_img}'/>" width="16" height="16"/>
                          </a>
                        </td>
                      </c:if>
                      <c:set var="j" value="0"/>

<%-- FOR EACH COLUMN --%>
                      <c:forEach items="${wdkAnswer.summaryAttributeNames}" var="sumAttrName">
                        <c:set value="${record.summaryAttributes[sumAttrName]}" var="recAttr"/>

<%--
<c:choose>
<c:when test = "${eupathIsolatesQuestion}">
												<imp:isolateClustal recAttr="${recAttr}" recNam="${recNam}" primaryKey="${primaryKey}"/>
</c:when>
<c:otherwise>
--%>
                         <imp:wdkAttribute attributeValue="${recAttr}" truncate="true" recordName="${recNam}" />
<%--
</c:otherwise>
</c:choose>
--%>
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
              <imp:pager wdkAnswer="${wdkAnswer}" pager_id="bottom"/> 
            </th>
          </tr>
        </table>
        <%--------- END OF PAGING BOTTOM BAR ----------%>
      </pg:pager>
    </c:otherwise> <%-- end of resultSize != 0 --%>
  </c:choose>
