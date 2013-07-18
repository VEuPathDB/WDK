<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set value="${requestScope.testForm}" var="testForm"/>

param A was: ${testForm.paramA}<br><br>
param B was: ${testForm.paramB}<br><br>
