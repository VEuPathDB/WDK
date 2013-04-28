<%@ taglib uri="http://jsptags.com/tags/navigation/pager" prefix="pg" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>

<%@ attribute name="pager_id"
              required="true"
              description="the unique identifier for the included pager"
%>

<%@ attribute name="wdkAnswer"
              required="true"
              type="org.gusdb.wdk.model.jspwrap.AnswerValueBean"
              description="the AnswerValueBean for this pager"
%>
<c:set var="resultSize" value="${wdkAnswer.resultSize}" />

<pg:page>
  <c:set var="advancedPagingUrl" value="${pageUrl}"/>
</pg:page>

  <table border="0">
    <tr>
      <td nowrap>
      
<pg:index>
  <pg:first>
    <a href="javascript:wdk.resultsPage.GetResultsPage('${pageUrl}',true,true)">First</a>
  </pg:first>

  <pg:prev>
    <a href="javascript:wdk.resultsPage.GetResultsPage('${pageUrl}',true,true)">Previous</a>
  </pg:prev>

  <pg:pages>
    <c:set var="pageDistance" value="${currentPageNumber - pageNumber}" />
    <c:if test="${pageDistance lt 5 and pageDistance gt -5}">
      <c:choose>
        <c:when test="${pageNumber eq currentPageNumber}">
          <b>${pageNumber}</b>
        </c:when>
        <c:otherwise>
          <a href="javascript:wdk.resultsPage.GetResultsPage('${pageUrl}',true,true)">${pageNumber}</a>
        </c:otherwise>
      </c:choose>
    </c:if>
  </pg:pages>

  <pg:next>
    <a href="javascript:wdk.resultsPage.GetResultsPage('${pageUrl}',true,true)">Next</a>
  </pg:next>

  <pg:last>
    <a href="javascript:wdk.resultsPage.GetResultsPage('${pageUrl}',true,true)">Last</a>
    
  </pg:last>
</pg:index>
</td>
     
<%-- 'All' link to display all results, if the result set is not too big --%>
	<td>
	    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	    <input class="paging-button" style="width:150px" type="button" value="Advanced Paging" onclick="wdk.resultsPage.openAdvancedPaging(this)"/>
			<div class="advanced-paging" title="Advanced Paging">
        <input type="hidden" id="resultSize" class="resultSize" value="${resultSize}" />
        <div class="text">Jump To page:</div>
        <input type="text" id="${pager_id}_pageNumber" class="pageNumber" size="5" value="${currentPageNumber}"/>
        <input type="hidden" id="pageUrl" class="pageUrl" value="${advancedPagingUrl}" />
        [1..<span id="${pager_id}_pageCount" class="pageCount">${wdkAnswer.pageCount}</span>]
        <hr/>
        <div class="text">Results Per Page:</div>
	      <!-- display the choice of page size -->
	      <select id="${pager_id}_pageSize" class="pageSize" onchange="wdk.resultsPage.updatePageCount(this);">
	          <option value="5" ${(wdk_paging_pageSize == 5)? 'SELECTED' : ''}>5</option>
	          <option value="10" ${(wdk_paging_pageSize == 10)? 'SELECTED' : ''}>10</option>
	          <option value="20" ${(wdk_paging_pageSize == 20)? 'SELECTED' : ''}>20</option>
	          <option value="50" ${(wdk_paging_pageSize == 50)? 'SELECTED' : ''}>50</option>
	          <option value="100" ${(wdk_paging_pageSize== 100)? 'SELECTED' : ''}>100</option>
	          <option value="500" ${(wdk_paging_pageSize == 500)? 'SELECTED' : ''}>500 (slow)</option>
	          <option value="1000" ${(wdk_paging_pageSize== 1000)? 'SELECTED' : ''}>1000 (very slow)</option>
	      </select>
	      <input class="submit" type="button" value="GO" onclick="wdk.resultsPage.closeAdvancedPaging(this); wdk.resultsPage.gotoPage(this);"/>
      </div>
    </td>
  </tr>
</table> 

   
  
