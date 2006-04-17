<%@ taglib prefix="site" tagdir="/WEB-INF/tags/site" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>

<!-- get wdkQuestion; setup requestScope HashMap to collect help info for footer -->  
<c:set value="${sessionScope.wdkQuestion}" var="wdkQuestion"/>
<jsp:useBean scope="request" id="helps" class="java.util.HashMap"/>

<!-- display page header with wdkQuestion displayName as banner -->
<site:header banner="${wdkQuestion.displayName}" />

<!-- display description for wdkQuestion -->
<p><b><jsp:getProperty name="wdkQuestion" property="description"/></b></p>

<hr>

<!-- show all params of question, collect help info along the way -->
<c:set value="Help for question: ${wdkQuestion.displayName}" var="fromAnchorQ"/>
<jsp:useBean id="helpQ" class="java.util.HashMap"/>

<!-- put an anchor here for linking back from help sections -->
<A name="${fromAnchorQ}"></A>
<html:form method="get" action="/processQuestion.do">
<input type="hidden" name="questionFullName" value="${wdkQuestion.fullName}"/>
<table>

<!-- show error messages, if any -->
<wdk:errors/>

<c:set value="${wdkQuestion.params}" var="qParams"/>
<c:forEach items="${qParams}" var="qP">

  <!-- an individual param (can not use fullName, w/ '.', for mapped props) -->
  <c:set value="${qP.name}" var="pNam"/>
  <tr><td align="right"><b><jsp:getProperty name="qP" property="prompt"/></b></td>

  <!-- choose between flatVocabParam and straight text or number param -->
  <c:choose>
    <c:when test="${qP.class.name eq 'org.gusdb.wdk.model.jspwrap.FlatVocabParamBean'}">
      <td>
        <c:set var="opt" value="0"/>

        <c:choose>
          <c:when test="${qP.multiPick}">
            <!-- multiPick is true, use scroll pane -->
            <html:select  property="myMultiProp(${pNam})" multiple="1">
              <c:set var="opt" value="${opt+1}"/>
              <c:set var="sel" value=""/>
              <c:if test="${opt == 1}"><c:set var="sel" value="selected"/></c:if>      
              <html:options property="values(${pNam})" labelProperty="labels(${pNam})"/>
            </html:select>
          </c:when>
          <c:otherwise>
            <!-- multiPick is false, use pull down menu -->
            <html:select  property="myMultiProp(${pNam})">
              <c:set var="opt" value="${opt+1}"/>
              <c:set var="sel" value=""/>
              <c:if test="${opt == 1}"><c:set var="sel" value="selected"/></c:if>      
              <html:options property="values(${pNam})" labelProperty="labels(${pNam})"/>
            </html:select>
          </c:otherwise>
        </c:choose>
      </td>
    </c:when>
    <c:otherwise>
      <td>
        <html:text property="myProp(${pNam})"/>
      </td>
    </c:otherwise>
  </c:choose>

      <td>&nbsp;&nbsp;&nbsp;&nbsp;</td>
      <td>
          <c:set var="anchorQp" value="HELP_${fromAnchorQ}_${pNam}"/>
          <c:set target="${helpQ}" property="${anchorQp}" value="${qP}"/>
          <a href="#${anchorQp}">
          <img src='<c:url value="/images/toHelp.jpg"/>' border="0" alt="Help!"></a>
      </td>
  </tr>
</c:forEach>
<c:set target="${helps}" property="${fromAnchorQ}" value="${helpQ}"/>

  <tr><td></td>
      <td><html:submit property="questionSubmit" value="Get Answer"/></td>
      <td><html:submit property="questionSubmit" value="Expand Question"/></td>
</table>
</html:form>



<site:footer/>
