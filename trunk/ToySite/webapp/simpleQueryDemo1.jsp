<%@ taglib prefix="sample" tagdir="/WEB-INF/tags/local" %>
<%@ taglib prefix="wdkq" uri="http://www.gusdb.org/taglibs/wdk-query-0.1" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<sample:header banner="Simple Query 1" />
<p>This page has a simple, non-boolean query. No query is
  specified so you should get a list of possible queries.
  <hr><p>
      <table>
        <wdkq:queryHolder name="form1"
        querySet="RNASimpleQueries"
        var="q">
          <input type="hidden" name="fromPage" value="/simpleQueryDemo1.jsp">
            <tr><td><wdkq:displayQuery queryInstance="${q}"></td></tr>
            <c:forEach var="p"
            items="${q.query.params}">
              <tr>
                <td><b>${p.prompt}</b></td>
                <td><wdkq:displayParam param="${p}" /></td>
                <td>${p.help}</td>
              </tr>
            </c:forEach>
            </wdkq:displayQuery>
          <tr>
            <td><wdkq:submit /></td>
            <td><wdkq:reset /></td>
          </tr>
        </wdkq:queryHolder>
      </table>
      <sample:footer />
