<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<hr>

<font class="footer">
This is a sample site demonstrating WebDevKit. The data may not
be accurate or complete. Please see <a href="http://www.allgenes.org">Allgenes</a> or 
<a href="http://www.genedb.org/">GeneDB</a> for full-fledged sites.
</font>


<c:if test="${!empty helps}">
  <BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR>
  <BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR><BR>

  <TABLE cellpadding="0" width="100%" border="0" cellspacing="2">
    <TR><TD bgcolor="#000000"><FONT size="+1" color="#ffffff">&nbsp;<B>Help</B></FONT></TD></TR>
    <TR><TD>&nbsp;</TD></TR>
  </TABLE>

  <TABLE width="100%" border="0">

  <!-- help for one form -->
  <c:forEach items="${helps}" var="hlp">
    <TR><TD valign="middle" bgcolor="#e0e0e0" align="left">
          <FONT size="+0" color="#663333" face="helvetica,sans-serif">
          <B>${hlp.key}</B></FONT></TD></TR>
    <TR><TD><TABLE width="100%">

            <!-- help for one param -->
            <c:forEach items="${hlp.value}" var="hlpLn">
            <TR><TD align="left"><B><A name="${hlpLn.key}"></A>${hlpLn.value.prompt}</B></TD>
                <TD align="right"><A href="#${hlp.key}">
                    <IMG src='<c:url value="/images/fromHelp.jpg"/>' alt="Back To Form" border="0"></A>
                </TD></TR>
            <TR><TD colspan="2">${hlpLn.value.help}</TD></TR>
            <TR><TD colspan="2">&nbsp;</TD></TR>
            </c:forEach>
            </TABLE>
        </TD></TR> 
  </c:forEach>
  </TABLE>
</c:if>

</body></html>
