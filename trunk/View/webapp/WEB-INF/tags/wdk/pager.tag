<%@ taglib uri="http://jsptags.com/tags/navigation/pager" prefix="pg" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>

<%@ attribute name="pager_id"
              required="true"
              description="the unique identifier for the included pager"
%>

<c:set var="resultSize" value="${wdkAnswer.resultSize}" />

<input type="hidden" id="resultSize" value="${resultSize}" />

<pg:index>
  <table border="0">
    <tr>
      <td nowrap>
      
  <pg:first>
    <a href="javascript:GetResultsPage('${pageUrl}',true,true)">First</a>
  </pg:first>

  <pg:prev>
    <a href="javascript:GetResultsPage('${pageUrl}',true,true)">Previous</a>
  </pg:prev>

  <pg:pages>
    <c:set var="pageDistance" value="${currentPageNumber - pageNumber}" />
    <c:if test="${pageDistance < 5 && pageDistance > -5}">
      <c:choose>
        <c:when test="${pageNumber==currentPageNumber}">
          <b>${pageNumber}</b>
        </c:when>
        <c:otherwise>
          <a href="javascript:GetResultsPage('${pageUrl}',true,true)">${pageNumber}</a>
        </c:otherwise>
      </c:choose>
    </c:if>
  </pg:pages>

  <pg:next>
    <a href="javascript:GetResultsPage('${pageUrl}',true,true)">Next</a>
  </pg:next>

  <pg:last>
    <a href="javascript:GetResultsPage('${pageUrl}',true,true)">Last</a>
    
  </pg:last>
  
      </td>
     
   <%-- 'All' link to display all results, if the result set is not too big --%>
      <td>
		&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                <input class="paging-button" style="width:150px" type="button" value="Advanced Paging" onclick="openAdvancedPaging(this)"/>
		<div class="advanced-paging" style="">
			<span id="CAP" onclick="openAdvancedPaging($(this).parent().prev())">[x]</span>
        <pg:page>
          Jump To page:
          <input type="text" id="${pager_id}_pageNumber" size="5" value="${currentPageNumber}"/>
          <input type="hidden" id="pageUrl" value="${pageUrl}" />
          <font size="-1">
            [1..<span id="${pager_id}_pageCount">${wdkAnswer.pageCount}</span>]
             <br/>Results Per Page:
          </font>
          
          <!-- display the choice of page size -->
          <select id="${pager_id}_pageSize" onchange="updatePageCount('${pager_id}');">
             <option value="5" ${(wdk_paging_pageSize == 5)? 'SELECTED' : ''}>5</option>
             <option value="10" ${(wdk_paging_pageSize == 10)? 'SELECTED' : ''}>10</option>
             <option value="20" ${(wdk_paging_pageSize == 20)? 'SELECTED' : ''}>20</option>
             <option value="50" ${(wdk_paging_pageSize == 50)? 'SELECTED' : ''}>50</option>
             <option value="100" ${(wdk_paging_pageSize== 100)? 'SELECTED' : ''}>100</option>
             <option value="500" ${(wdk_paging_pageSize == 500)? 'SELECTED' : ''}>500 (slow)</option>
             <option value="1000" ${(wdk_paging_pageSize== 1000)? 'SELECTED' : ''}>1000 (very slow)</option>
           </select>
           <input type="button" value="GO" onclick="gotoPage('${pager_id}');"/>
        
        </pg:page>
       </div>
      </td>
    </tr>
  </table> 

   
</pg:index>
  
