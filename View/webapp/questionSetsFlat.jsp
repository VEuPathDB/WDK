<%@ taglib prefix="site" tagdir="/WEB-INF/tags/site" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>

<!-- get wdkModel saved in application scope -->
<c:set var="wdkModel" value="${applicationScope.wdkModel}"/>

<!-- get wdkModel name to display as page header -->
<c:set value="${wdkModel.displayName}" var="wdkModelDispName"/>
<site:header banner="${wdkModelDispName}" />

<!-- display wdkModel introduction text -->
<b><jsp:getProperty name="wdkModel" property="introduction"/></b>

<hr>

<jsp:useBean scope="request" id="helps" class="java.util.HashMap"/>

<!-- show error messages, if any -->
<wdk:errors/>

<!-- show all questionSets in model -->
<table width="100%">
<c:set value="${wdkModel.questionSets}" var="questionSets"/>
<c:forEach items="${questionSets}" var="qSet">
  <c:if test="${qSet.isInternal == false}">
    <c:set value="${qSet.name}" var="qSetName"/>
    <c:set value="${qSet.questions}" var="questions"/>
    <!-- list of questions in a questionSet -->
    <c:forEach items="${questions}" var="q">
      <c:set value="${q.name}" var="qName"/>
      <c:set value="${q.displayName}" var="qDispName"/>
      <tr><td><!-- display description for wdkQuestion -->
        <p><b><jsp:getProperty name="q" property="description"/></b></p>

          <html:form method="get" action="/processQuestionSetsFlat.do">
          <html:hidden property="questionFullName" value="${qSetName}.${qName}"/>
          <!-- show all params of question, collect help info along the way -->
          <c:set value="Help for question: ${q.displayName}" var="fromAnchorQ"/>
          <jsp:useBean id="helpQ" class="java.util.HashMap"/>

          <!-- put an anchor here for linking back from help sections -->
          <A name="${fromAnchorQ}"></A>
          <table>
            <c:set value="${q.params}" var="qParams"/>
            <c:forEach items="${qParams}" var="qP">
            <!-- an individual param (can not use fullName, w/ '.', for mapped props) -->
              <c:set value="${qP.name}" var="pNam"/>
              <c:set value="${qSetName}_${qName}_${pNam}" var="pNamKey"/>           
              <tr><td align="right"><b><jsp:getProperty name="qP" property="prompt"/></b></td>

                <!-- choose between flatVocabParam and straight text or number param -->
                <c:choose>
                <c:when test="${qP.class.name eq 'org.gusdb.wdk.model.jspwrap.FlatVocabParamBean'}">
                  <td>
                  <c:set var="opt" value="0"/>
                  <c:choose>
                    <c:when test="${qP.multiPick}">
                    <!-- multiPick is true, use scroll pane -->
                    <html:select  property="myMultiProp(${pNamKey})" multiple="1">
                      <c:set var="opt" value="${opt+1}"/>
                      <c:set var="sel" value=""/>
                      <c:if test="${opt == 1}"><c:set var="sel" value="selected"/></c:if>      
                      <html:options property="values(${pNamKey})" labelProperty="labels(${pNamKey})"/>
                    </html:select>
                    </c:when>
                    <c:otherwise>
                      <!-- multiPick is false, use pull down menu -->
                      <html:select  property="myMultiProp(${pNamKey})">
                        <c:set var="opt" value="${opt+1}"/>
                        <c:set var="sel" value=""/>
                        <c:if test="${opt == 1}"><c:set var="sel" value="selected"/></c:if>      
                        <html:options property="values(${pNamKey})" labelProperty="labels(${pNamKey})"/>
                      </html:select>
                    </c:otherwise>
                  </c:choose>
                  </td>
                </c:when>
                <c:otherwise>
                <td>
                  <html:text property="myProp(${pNamKey})"/>
                </td>
                </c:otherwise>
                </c:choose>

                <td>&nbsp;&nbsp;&nbsp;&nbsp;</td>
                <td>
                  <c:set var="anchorQp" value="HELP_${fromAnchorQ}_${pNamKey}"/>
                  <c:set target="${helpQ}" property="${anchorQp}" value="${qP}"/>
                  <a href="#${anchorQp}">
                  <img src='<c:url value="/images/toHelp.jpg"/>' border="0" alt="Help!"></a>
                </td>
              </tr>
            </c:forEach>
            <c:set target="${helps}" property="${fromAnchorQ}" value="${helpQ}"/>

            <tr><td></td>
               <td><html:submit property="questionSubmit" value="Get Answer"/></td>
               <td><html:submit property="questionSubmit" value="Expand Question"/></td></tr>
          </table>
          </html:form>

        </td></tr>
        <tr bgcolor="lightblue"><td></td></tr>
      </c:forEach>
   </c:if>
</c:forEach>

</table>

<site:footer/>
