<%@ taglib uri="http://jsptags.com/tags/navigation/pager" prefix="pg" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>

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

        <!-- form method="get" action="" -->
          &nbsp;&nbsp;Go to page: 
          <input type="text" id="pageNumber" name="pageNumber" size="5" value="${currentPageNumber}"/>
          <font size="-1">[1 ... ${wdkAnswer.pageCount}]</font>
          <input type="button" name="questionSubmit" value="GO" onclick="gotoPage()"/>
        <!--/form -->

        <script language="JavaScript">
        <!--
           function gotoPage() {
              //alert("hello");
              var pageNumber = document.getElementById("pageNumber").value;
              var pageSize = "${wdk_paging_pageSize}";
              var pageOffset = (pageNumber - 1) * pageSize;
              var gotoPageUrl = "${pageUrl}".replace(/\&pager\.offset=\d+/, "")
              gotoPageUrl += "&pager.offset=" + pageOffset;
              //alert("&pager.offset=0".replace(/\&pager\.offset=\d+/, ""));
              window.location.href = gotoPageUrl;
           }
        //-->
        </script>
        
        </pg:page>
      </td>
    </tr>
  </table> 

   
</pg:index>
  
