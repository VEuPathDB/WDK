<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="baskets" value="${requestScope.baskets}" />

<ul class="menubar">
  <c:set var="typeC" value="0" />
  <c:forEach items="${baskets}" var="basket">
    <c:if test="${basket.resultSize > 0}">
      <c:set var="typeC" value="${typeC+1}"/>
      <c:if test="${typeC != 1}">
        <li>|</li>
      </c:if>
      <li>
        <a id="tab_${basket.answerValue.recordClass.shortDisplayName}"
           onclick="showBasket('${basket.answerValue.recordClass.fullName}','${basket.answerValue.recordClass.shortDisplayName}')"
           href="javascript:void(0)">${basket.answerValue.recordClass.displayName}&nbsp;(${basket.resultSize})</a>
      </li>
    </c:if>
  </c:forEach>
</ul>
