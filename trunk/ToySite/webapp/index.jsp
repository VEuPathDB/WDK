<%@ taglib prefix="sample" tagdir="/WEB-INF/tags/local" %>
<%@ taglib prefix="wdkq" uri="http://www.gusdb.org/taglibs/wdk-query-0.1" %>
<%@ taglib prefix="wdkm" uri="http://www.gusdb.org/taglibs/wdk-misc-0.1" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<sample:header banner="Simple Query 1" />


<p><b><wdkm:modelIntroduction /></b>
<p>
  
  <wdkm:model var="model">
    <c:forEach var="questionSet" varStatus="status" items="${model.allSummarySets}">
 
 <hr> 
      <table>
        <tr><td><b>${questionSet.name}</b></td></tr>
        <wdkq:queryHolder name="form${status.count}"
                          questionSetName="${questionSet.name}"
                          var="q">
          <input type="hidden" name="fromPage" value="/index.jsp">
            <tr><td>${q.fullName}<br><wdkq:displayQuery question="${q}"></td></tr>
            <c:forEach var="p"
            items="${q.query.params}">
              <tr>
                <td align="right"><b>${p.prompt}</b></td>
                <td><wdkq:displayParam param="${p}" /></td>
                <td>${p.help}</td>
              </tr>
            </c:forEach>
            </wdkq:displayQuery>
          <tr>
            <td><wdkq:submit>Go</wdkq:submit></td>
          </tr>
        </wdkq:queryHolder>
      </table>      
      
      </c:forEach>
   </wdkm:model>
      
      <sample:footer />
