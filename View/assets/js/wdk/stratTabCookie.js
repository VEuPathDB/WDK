//wdk.util.namespace("window.wdk.stratTabCookie", function(ns, $) {
define(["jquery", "exports", "module"], function($, ns, module) {
  "use strict";

  //CONSTANTS
  var currentTabCookie = "wdk_tab_state";

  function getCurrentTabCookie(type) {
    var cookieValue = $.cookie(currentTabCookie);
    var tabStates;
    if (cookieValue != null) tabStates = cookieValue.split(type + "=")[1];
    if (tabStates) {
      return tabStates.split("&")[0];
    }
  }

  function setCurrentTabCookie(type, value) {
    var oldValue = $.cookie(currentTabCookie);
    var newValue, tabStates;
    if (oldValue != null) tabStates = oldValue.split(type + "=");
    if (tabStates && tabStates.length > 1) {
      newValue = tabStates[0] + type + "=" + value;
      if (tabStates[1].indexOf("&") >= 0) {
        newValue += tabStates[1].substring(tabStates[1].indexOf("&"));
      }
    } else {
      newValue = oldValue + "&" + type + "=" + value;
    }

    $.cookie(currentTabCookie, newValue, { path : '/' });
    return true; //so that href will be followed when setting cookie in a link's onclick attr
  }

  ns.getCurrentTabCookie = getCurrentTabCookie;
  ns.setCurrentTabCookie = setCurrentTabCookie;

});
