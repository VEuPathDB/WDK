<?xml version="1.0" encoding="UTF-8"?>

<jsp:root version="2.0"
  xmlns:jsp="http://java.sun.com/JSP/Page"
  xmlns:c="http://java.sun.com/jsp/jstl/core"
  xmlns:imp="urn:jsptagdir:/WEB-INF/tags/imp">

  <jsp:directive.attribute name="refer" required="false" 
              description="Page calling this tag. The list of WDK recognized refer values are: home, question, summary, record"/>

  <jsp:directive.attribute name="min"
      type="java.lang.Boolean"
      required="false"/>

  <!-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
  <!-- Scripts and styles that are used on the whole site                    -->
  <!-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->

  <!-- WDK libraries and source files -->
  <!-- see WDK/View/assets/wdkFiles.js for details about these files -->
  <imp:script src="/wdk/wdk.libs.js"/>
  <imp:script src="/wdk/wdk.js"/>

  <!-- TODO - move these calls to data-controller invokation -->
  <!-- All pages -->
  <script>
    jQuery(wdk.init);
    jQuery(wdk.user.init);
    jQuery(wdk.favorite.init);
  </script>

  <!-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
  <!-- scripts and styles used on the QUESTION page. all question content are 
       also included in summary page to support the addStep popup            -->
  <!-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
  <c:if test="${refer == 'question' || refer == 'summary'}">
  <!--
    <script>
      jQuery(wdk.question.init);
      jQuery(wdk.parameterHandlers.init);
    </script>
  -->
  </c:if>


  <!-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
  <!-- scripts and styles used on the SUMMARY page only                      -->
  <!-- +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
  <c:if test="${refer == 'summary'}">

    <!-- JQuery Drag And Drop Plugin -->
    <!--
    <imp:script src="/wdk/js/lib/jquery.multiSelect.js"/>
    <imp:script src="/wdk/js/lib/jquery.form.js"/>
    -->

    <!-- TODO - add async loading capabilities for not-commonly used libraries -->
    <!-- Results Page  -->
    <imp:script src="/wdk/js/lib/flexigrid.js"/>

    <!-- TODO - move these calls to data-controller invokation -->
    <script>
      jQuery(wdk.step.init);
      jQuery(wdk.strategy.controller.init);
      jQuery(wdk.wordCloud.init);
    </script>

    <jsp:text><![CDATA[
    <!--[if lt IE 7]>
    <script type="text/javascript">
          jQuery(document).ready(function(){
                  $("#Strategies").prepend("<div style='height:124px;'>&nbsp;</div>");
          });
    </script>
    <![endif]-->
    ]]></jsp:text>

  </c:if>
</jsp:root>
