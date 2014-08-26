<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="imp" tagdir="/WEB-INF/tags/imp" %>

<c:set var="summary" value="${requestScope.summary}" />

<div class="filter-summary">

  <html:form action="/processFilter.do">
     <html:select>
       <c:forEach items="${summary.strategies}" var="strategy">
         <html:option value="${strategy.strategyId}">${strategy.name}</html:option>
       </c:forEach>
     </html:select>
   </html:form>
  
</div>

