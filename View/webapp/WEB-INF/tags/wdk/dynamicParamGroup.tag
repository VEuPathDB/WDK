<?xml version="1.0" encoding="UTF-8"?>

<!-- Override this tag to provide custom logic.
     Content in this tag appears within the group-detail DIV. -->

<jsp:root version="2.0"
  xmlns:jsp="http://java.sun.com/JSP/Page"
  xmlns:c="http://java.sun.com/jsp/jstl/core"
  xmlns:imp="urn:jsptagdir:/WEB-INF/tags/imp">

  <jsp:directive.attribute name="paramGroup" type="java.util.Map" required="true"/>

  <c:set var="wdkQuestion" value="${requestScope.wdkQuestion}"/>

  <imp:questionParamGroup paramGroup="${paramGroup}" />

</jsp:root>
