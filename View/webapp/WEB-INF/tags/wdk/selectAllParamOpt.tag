<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="2.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:c="http://java.sun.com/jsp/jstl/core"
    xmlns:fn="http://java.sun.com/jsp/jstl/functions">

  <jsp:directive.attribute name="enumParam" required="true"
    type="org.gusdb.wdk.model.jspwrap.EnumParamBean"
    description="Enum param to display warning for"/>

  <jsp:directive.attribute name="onchange" required="false"
    type="java.lang.String"
    description="Javascript code to call after new selection has been made (can be empty)"/>
  
  <c:set var="pNam" value="${enumParam.name}"/>
  
  <c:if test="${fn:length(enumParam.vocab) > 2}">
    <a class="small" href="javascript:void(0)" onclick="wdk.chooseAll(1, $(this).parents('form').get(0), 'array(${pNam})' ); ${onchange}">select all</a> |
    <a class="small" href="javascript:void(0)" onclick="wdk.chooseAll(0, $(this).parents('form').get(0), 'array(${pNam})' ); ${onchange}">clear all</a>
  </c:if>

</jsp:root>
