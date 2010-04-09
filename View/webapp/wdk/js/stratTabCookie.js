//CONSTANTS
var currentTabCookie = "current_application_tab";
var currentHistTabCookie = "current_history_tab";

function getCurrentTabCookie(isHist){
	if (isHist)
		return jQuery.cookie(currentHistTabCookie);
	else
		return jQuery.cookie(currentTabCookie);
}

function setCurrentTabCookie(value, isHist){
	if (isHist)
		jQuery.cookie(currentHistTabCookie, value, { path : '/' });
	else
		jQuery.cookie(currentTabCookie, value, { path : '/' });
	return true; //so that href will be followed when setting cookie in a link's onclick attr
}
