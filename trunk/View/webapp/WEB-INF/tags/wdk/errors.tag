<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="err" scope="request" value="${requestScope['org.apache.struts.action.ERROR']}"/>
<c:set var="exp" scope="request" value="${requestScope['org.apache.struts.action.EXCEPTION']}"/>

<c:if test="${err != null || exp != null}"><br><br></c:if>

<c:if test="${err != null}">${err}<br><br></c:if>

<c:if test="${exp != null}">
  Exception: ${exp} <br>
  Exception message: ${exp.message} <br>
  Stacktrace: <br>
  <c:forEach items="${exp.stackTrace}" var="st">
    ${st} <br>
  </c:forEach>
  <br><br>
</c:if>
