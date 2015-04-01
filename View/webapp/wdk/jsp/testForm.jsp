<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>
<%@ taglib prefix="logic" uri="http://struts.apache.org/tags-logic" %>

<html:errors/>

<html:form method="get" action="/processTestForm.do">
    <html:text property="paramA"/><br>
    <html:text property="paramB"/><br>

    <html:submit property="submit" value="GO"/></td>
</html:form>
