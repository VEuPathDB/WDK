<%@ taglib prefix="sample" tagdir="/WEB-INF/tags/local" %>

<sample:header banner="Simple Record Demo Driver" />

<p>This page has some links to test out record retrieval 
<hr><p>

<form method="GET" action="/sampleWDK/RecordTester">
<input type="hidden" name="recordSetName" value="RNARecords">
<input type="hidden" name="recordName" value="PSUCDSRecord">

<p>Please enter a <b>systematic id</b> for <i>S. pombe</i> <!-- or <i>A. fumigatus</i> -->
<input type="text" name="primaryKey" size="16">

<p>Style:
<input type="radio" name="style" value="plain">Plain
&nbsp;&nbsp;&nbsp;
<input type="radio" name="style" checked value="jsp">JSP

<p>
<input type="submit">
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<input type="reset"> 
</form>


<sample:footer />
