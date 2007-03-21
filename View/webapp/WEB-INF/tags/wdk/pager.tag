<%@ taglib uri="http://jsptags.com/tags/navigation/pager" prefix="pg" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>

<%@ attribute name="pager_id"
              required="true"
              description="the unique identifier for the included pager"
%>

<script language="JavaScript">
<!--

    function updatePageCount(pager_id) {
        var resultSize = ${wdkAnswer.resultSize};
        var psSelect = document.getElementById(pager_id + "_pageSize");
        var index = psSelect.selectedIndex;
        var pageSize = psSelect.options[index].value;
        var pageCount = Math.ceil(resultSize / pageSize);
        if (pageCount * pageSize < resultSize) pageCount++;
        var span = document.getElementById(pager_id + "_pageCount");
        span.innerHTML = pageCount;
    }
    
    function gotoPage(pager_id) {
        //alert("hello");
        var pageNumber = document.getElementById(pager_id + "_pageNumber").value;
        var psSelect = document.getElementById(pager_id + "_pageSize");
        var pageSize = psSelect.options[psSelect.selectedIndex].value;
        
        var pageUrl = document.getElementById("pageUrl").value;
        
        var pageOffset = (pageNumber - 1) * pageSize;
        var gotoPageUrl = pageUrl.replace(/\&pager\.offset=\d+/, "");
        gotoPageUrl = gotoPageUrl.replace(/\&pageSize=\d+/, "");
        gotoPageUrl += "&pager.offset=" + pageOffset;
        gotoPageUrl += "&pageSize=" + pageSize;
        window.location.href = gotoPageUrl;
    }
//-->
</script>


<pg:index>
  <table border="0">
    <tr>
      <td nowrap>
      
  <pg:first>
    <a href="${pageUrl}">First</a>
  </pg:first>

  <pg:prev>
    <a href="${pageUrl}">Previous</a>
  </pg:prev>

  <pg:pages>
    <c:set var="pageDistance" value="${currentPageNumber - pageNumber}" />
    <c:if test="${pageDistance <= 5 && pageDistance >= -5}">
      <c:choose>
        <c:when test="${pageNumber==currentPageNumber}">
          <b>${pageNumber}</b>
        </c:when>
        <c:otherwise>
          <a href="${pageUrl}">${pageNumber}</a>
        </c:otherwise>
      </c:choose>
    </c:if>
  </pg:pages>

  <pg:next>
    <a href="${pageUrl}">Next</a>
  </pg:next>

  <pg:last>
    <a href="${pageUrl}">Last</a>
    
  </pg:last>
  
      </td>
      <td nowrap>
        <pg:page>
          To page: 
          <input type="text" id="${pager_id}_pageNumber" size="5" value="${currentPageNumber}"/>
          <input type="hidden" id="pageUrl" value="${pageUrl}" />
          <font size="-1">
            [1..<span id="${pager_id}_pageCount">${wdkAnswer.pageCount}</span>]
            &nbsp;per page:
          </font>
          
          <!-- display the choice of page size -->
          <select id="${pager_id}_pageSize" onchange="updatePageCount('${pager_id}');">
             <option value="5" ${(wdk_paging_pageSize == 5)? 'SELECTED' : ''}>5</option>
             <option value="10" ${(wdk_paging_pageSize == 10)? 'SELECTED' : ''}>10</option>
             <option value="20" ${(wdk_paging_pageSize == 20)? 'SELECTED' : ''}>20</option>
             <option value="50" ${(wdk_paging_pageSize == 50)? 'SELECTED' : ''}>50</option>
             <option value="100" ${(wdk_paging_pageSize== 100)? 'SELECTED' : ''}>100</option>
           </select>
           <input type="button" value="GO" onclick="gotoPage('${pager_id}');"/>
        
        </pg:page>
      </td>
    </tr>
  </table> 

   
</pg:index>
  
