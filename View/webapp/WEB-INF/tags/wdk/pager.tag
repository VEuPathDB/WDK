<%@ taglib uri="http://jsptags.com/tags/navigation/pager" prefix="pg" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>


<script language="JavaScript">
<!--

    function updatePageCount() {
        var resultSize = ${wdkAnswer.resultSize};
        var psSelect = document.getElementById("pageSize");
        var pageSize = psSelect.options[psSelect.selectedIndex].value;
        var pageCount = Math.ceil(resultSize / pageSize);
        if (pageCount * pageSize < resultSize) pageCount++;
        var span = document.getElementById("pageCount");
        span.innerText = pageCount;
    }
    
    function gotoPage() {
        //alert("hello");
        var pageNumber = document.getElementById("pageNumber").value;
        var psSelect = document.getElementById("pageSize");
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
      <td>
      
  <pg:first>
    <a href="${pageUrl}">First</a>
  </pg:first>

  <pg:prev>
    <a href="${pageUrl}">Previous</a>
  </pg:prev>

  <pg:pages>
    <c:if test="${pageNumber < 10}">&nbsp;</c:if>
    <c:choose>
      <c:when test="${pageNumber==currentPageNumber}">
        <b>${pageNumber}</b>
      </c:when>
      <c:otherwise>
        <a href="${pageUrl}">${pageNumber}</a>
      </c:otherwise>
    </c:choose>
  </pg:pages>

  <pg:next>
    <a href="${pageUrl}">Next</a>
  </pg:next>

  <pg:last>
    <a href="${pageUrl}">Last</a>
    
  </pg:last>
  
      </td>
      <td>
        <pg:page>

          &nbsp;&nbsp;Go to page: 
          <input type="text" id="pageNumber" name="pageNumber" size="5" value="${currentPageNumber}"/>
          <input type="hidden" name="pageUrl" value="${pageUrl}" />
          <font size="-1">
            [1 ... <span id="pageCount">${wdkAnswer.pageCount}</span>]
            &nbsp;page size:
          </font>
          
          <!-- display the choice of page size -->
          <select name="pageSize" onchange="updatePageCount();">
             <option value="5" ${(wdk_paging_pageSize == 5)? 'SELECTED' : ''}>5</option>
             <option value="10" ${(wdk_paging_pageSize == 10)? 'SELECTED' : ''}>10</option>
             <option value="20" ${(wdk_paging_pageSize == 20)? 'SELECTED' : ''}>20</option>
             <option value="50" ${(wdk_paging_pageSize == 50)? 'SELECTED' : ''}>50</option>
             <option value="100" ${(wdk_paging_pageSize== 100)? 'SELECTED' : ''}>100</option>
           </select>
           <input type="button" name="questionSubmit" value="GO" onclick="gotoPage();"/>
        
        </pg:page>
      </td>
    </tr>
  </table> 

   
</pg:index>
  
