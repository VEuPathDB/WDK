//CONSTANTS
var currentTabCookie = "wdk_tab_state";

function getCurrentTabCookie(type){
	var cookieValue = jQuery.cookie(currentTabCookie);
	var tabStates;
	if (cookieValue != null) tabStates = cookieValue.split(type + "=")[1];
	if (tabStates) {
		return tabStates.split("&")[0];
	}
}

function setCurrentTabCookie(type, value){
	var oldValue = jQuery.cookie(currentTabCookie);
	var newValue, tabStates;
	if (oldValue != null) tabStates = oldValue.split(type + "=");
	if (tabStates && tabStates.length > 1) {
		newValue = tabStates[0] + type + "=" + value;
		if (tabStates[1].indexOf("&") >= 0) newValue += tabStates[1].substring(tabStates[1].indexOf("&"));
	}
	else {
		newValue = oldValue + "&" + type + "=" + value;
	}
	
	jQuery.cookie(currentTabCookie, newValue, { path : '/' });
	return true; //so that href will be followed when setting cookie in a link's onclick attr
}
