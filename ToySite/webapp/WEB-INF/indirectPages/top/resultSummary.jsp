<%@ taglib prefix="sample" tagdir="/WEB-INF/tags/local" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="misc" uri="http://www.gusdb.org/taglibs/wdk-misc-0.1" %>
<%@ taglib uri="http://jsptags.com/tags/navigation/pager" prefix="pg" %>


<sample:header title="Results Page" banner="Results" />

  <c:choose>
   <c:when test='${wdk.paging.total == 0}'>
      No results for your query
   </c:when>
   <c:otherwise>

     <pg:pager isOffset="true"
               scope="request"
               items="${wdk_paging_total}"
               maxItems="${wdk_paging_total}"
               url="${wdk_paging_url}"
               maxPageItems="${wdk_paging_pageSize}"
               >
     <c:forEach var="paramName" items="${wdk_paging_params}">
       <pg:param name="${paramName}" id="pager" />
     </c:forEach>
     
     <sample:pager /> 
     
     <c:import url="/WEB-INF/indirectPages/subviews/${renderer}.jsp" />

     <sample:pager />

  </pg:pager>
   </c:otherwise>
  </c:choose>
  <sample:footer />
