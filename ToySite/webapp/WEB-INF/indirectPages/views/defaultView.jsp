<%@ taglib prefix="site" tagdir="/WEB-INF/tags/site" %>
<%@ taglib prefix="wdkr" uri="http://www.gusdb.org/taglibs/wdk-report-0.1" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="misc" uri="http://www.gusdb.org/taglibs/wdk-misc-0.1" %>

<site:header banner="${ri._record.type} ${ri.primary_key}" />


<misc:table>
  <c:forEach var="entry" items="${ri}">
    <tr>
      <td><b>${ri._displayName[entry.key]}</b></td>
      <td><misc:multiType value="${entry.value}">${entry.value}</misc:multiType></td></tr>
  </c:forEach>
</misc:table>

<site:footer />
