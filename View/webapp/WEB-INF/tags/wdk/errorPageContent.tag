<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="2.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:c="http://java.sun.com/jsp/jstl/core"
    xmlns:fn="http://java.sun.com/jsp/jstl/functions"
    xmlns:imp="urn:jsptagdir:/WEB-INF/tags/imp"
    xmlns:wdkfn="http://gusdb.org/wdk/functions">
	<c:set var="wdkModel" value="${applicationScope.wdkModel}"/>
	<c:set var="projectName" value="${wdkModel.name}"/>
	<c:set var="supportEmail" value="${wdkModel.model.modelConfig.supportEmail}"/>
	<div id="error-main" style="margin-top:0px; padding-top:30px;">
	  <h1>We apologize...</h1>
	  <a style="border:none" href="javascript:ErrorUtils.playSadTrombone()">
	    <img class="error-image" src="${pageContext.request.contextPath}/wdkCustomization/images/sad-face.jpg"/>
	  </a>
	  <p>
	    An error has occurred and we cannot perform the action you requested.
	    If you would like to submit details about this error to the ${projectName} team, please
	    <a href="javascript:ErrorUtils.submitError()">click here.</a>
	  </p>
	  <p>
	    Thanks for your understanding.  If you would like correspondence about this error,
	    always feel free to email us at <a href="mailto:${supportEmail}">${supportEmail}</a>.
	  </p>
	  <p>
	    <span><a href="${referrerUrl}">Return to Previous Page</a></span>
	  </p>
	</div>
	<c:set var="strutsError" scope="request" value="${requestScope['org.apache.struts.action.ERROR']}"/>
  <c:set var="strutsException" scope="request" value="${requestScope['org.apache.struts.action.EXCEPTION']}"/>
  <c:set var="pageException" scope="request" value="${pageContext.exception}"/>
  <c:set var="errorContent">
    Project: ${projectName}
    Referring Page: ${exceptionPage}
    User: ${exceptionUser.email}
    <c:if test="${not empty exceptionObj}">
      Action Exception:
        ${wdkfn:getStackTrace(exceptionObj)}
    </c:if>
    <c:if test="${not empty strutsError}">
      Struts Error:
        ${wdkfn:getMessages(strutsError)}
    </c:if>
    <c:if test="${not empty strutsException}">
      Struts Exception:
        ${wdkfn:getStackTrace(strutsException)}
    </c:if>
    <c:if test="${not empty pageException}">
      Page Exception:
        ${wdkfn:getStackTrace(pageException)}
    </c:if>
  </c:set>
	<div>
	  <a href="javascript:ErrorUtils.toggleErrorDetails()">
	    <span id="exception-details-link" class="tiny-text">Show Details</span>
	  </a>
	  <div id="exception-information" style="display:none">
	    <pre>
	      ${errorContent}
	    </pre>
	  </div>
	</div>
	<div>
	  <form id="error-submission-form" method="post" action="contactUs.do">
	    <input type="hidden" name="subject" value="WDK Error Submission From User"/>
	    <input type="hidden" name="reply" value=""/>
	    <input type="hidden" name="addCc" value=""/>
	    <input type="hidden" name="content" value="${fn:escapeXml(errorContent)}"/>
	  </form>
	</div>
	<div style="display:none">
	  <a id="open-error-thanks-link" href="javascript:void()" class="open-dialog-error-submitted">_</a>
	</div>
	<div style="display:none" id="wdk-dialog-error-submitted" title="Thank you!">
	  <div class="popup-dialog">
	    <h2>Thank you!</h2>
	    <p>
	      Your error and what you were doing has been submitted to our development team.
	      Thank you for helping make our site better!
	    </p>
	  </div>
	</div>
</jsp:root>
