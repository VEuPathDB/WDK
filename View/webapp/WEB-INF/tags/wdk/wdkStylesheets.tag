<?xml version="1.0" encoding="UTF-8"?>

<jsp:root version="2.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:c="http://java.sun.com/jsp/jstl/core"
    xmlns:imp="urn:jsptagdir:/WEB-INF/tags/imp">

  <jsp:directive.attribute name="refer" required="false"
              description="Page calling this tag. The list of WDK recognized refer values are: home, question, summary, record"/>
  <jsp:directive.attribute name="debug" required="false" description="Use unminified files"/>

  <link rel="stylesheet" href="//maxcdn.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css"/>
  <imp:stylesheet rel="stylesheet" type="text/css" href="vendor.bundle.css"/>
  <imp:stylesheet rel="stylesheet" type="text/css" href="wdk-client.bundle.css"/>
</jsp:root>
