<%@ page contentType="text/html" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="bean" uri="http://jakarta.apache.org/struts/tags-bean" %>
<%@ taglib prefix="imp" tagdir="/WEB-INF/tags/imp" %>

<c:set var="summary" value="${requestScope.summary}" />
<c:set var="step" value="${requestScope.step}" />
<c:set var="maxCount" value="${summary.maxCount}" />

<div class="list-filter" data-max="${maxCount}">
  <div class="description">
    Please choose from the list to filter the result.
  </div>
  <table>
    <c:forEach items="${summary.counts}" var="item">
      <tr class="item-count">
        <c:set var="width" value="${100.0 * item.value / maxCount}" />
        <td><input name="values" type="checkbox" value="${item.key}" checked="checked" </td>
        <td class="item">${item.key}</td>
        <td class="count">${item.value}</td>
        <td width="200"><div class="count-bar" style="width:${width}%"> </div></td>
      </tr>
  </c:forEach>
  </table>
</div>
