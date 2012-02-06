<%@ page contentType="text/html" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="bean" uri="http://jakarta.apache.org/struts/tags-bean" %>
<%@ taglib prefix="imp" tagdir="/WEB-INF/tags/imp" %>

<c:set var="attribute" value="${requestScope.attribute}" />
<c:set var="plugin" value="${requestScope.plugin}" />
<c:set var="summary" value="${requestScope.summary}" />
<c:set var="histogram" value="${requestScope.histogram}" />

<script type="text/javascript">
$(document).ready(function() {
    $('#histogram').dataTable( {
        "bJQueryUI": true,
        "bPaginate": false,
        "aoColumns": [ null, null, { "bSortable": false } ],
        "aaSorting": [[ 0, "asc" ]],
        "sDom": 'lrti'
    } );
} );
</script>

<h2 align="center">${plugin.display}</h2>
<table id="histogram" class="datatables">
  <thead>
    <tr>
      <th>${attribute.displayName}</th>
      <th>#Records</th>
      <th>histogram</th>
    </tr>
  </thead>
  <tbody>
  <c:forEach items="${histogram}" var="item">
    <tr>
      <td>${item.key}</td>
      <td>${summary[item.key]}</td>
      <td><div class="bar" style="width:${item.value}px"> </div></td>
    </tr>
  </c:forEach>
  </tbody>
<c:if test="${fn:length(histogram) > 10}">
  <tfoot>
    <tr>
      <th>${attribute.displayName}</th>
      <th>#Records</th>
      <th>histogram</th>
    </tr>
  </tfoot>
</c:if>
</table>
