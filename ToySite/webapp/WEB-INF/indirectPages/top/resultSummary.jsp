<%@ taglib prefix="sample" tagdir="/WEB-INF/tags/local" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="misc" uri="http://www.gusdb.org/taglibs/wdk-misc-0.1" %>
<sample:header title="Results Page" banner="Results" />

  <c:choose>
   <c:when test='${siTotalSize == 0}'>
      No results for your query
   </c:when>
   <c:otherwise>
     <c:import url="/WEB-INF/subviews/${renderer}.jsp" />
   </c:otherwise>
  </c:choose>
  <sample:footer />
