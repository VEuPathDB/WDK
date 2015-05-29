/* jshint ignore:start */

// FIXME Review module
// Some redundant functions, some undefined functions called, etc.

wdk.util.namespace("window.wdk.user", function(ns, $) {
  "use strict";

  //var userData;

  // init function
  // values are set in wdkConfig global var in siteInfo.tag
  //ns.init = function() {
  //  userData = $("#wdk-user").data();
  //};

  ns.id = function() { return wdkConfig.wdkUser.id; };
  ns.name = function() { return wdkConfig.wdkUser.name; };
  ns.country = function() { return wdkConfig.wdkUser.country; };
  ns.email = function() { return wdkConfig.wdkUser.email; };
  ns.isGuest = function() { return wdkConfig.guestUser; };
  ns.isUserLoggedIn = function() { return !ns.isGuest(); };

  // var to hold reference to login dialog
  ns._dialog = null;

  ns.populateUserControl = function(userData) {
      // populate data field
    var loggedInValue = (userData.isLoggedIn ? "true" : "false");
      $('#login-status').attr('data-logged-in', loggedInValue);
    
    // populate visible section
    var templateSelector = (userData.isLoggedIn ?
        "#user-logged-in" : "#user-not-logged-in");
    var html = $(templateSelector).html();
    var template = Handlebars.compile(html);
    var html2 = template(userData);
    $('#user-control').html(html2);
  };

  ns.login = function(redirectUrl) {
    //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    // OLD: used to pop login form in dialog and then hijack form submit
    //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    //$("#wdk-dialog-login-form").dialog("open")
    //.find("input[name='redirectUrl']").val(redirectUrl);

    //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    // NEW: default WDK behavior now pops iframe with OAuth redirect in it
    //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

    // build URL to OAuth service
    var oauthServerBase = 'https://integrate.eupathdb.org/oauth';
    var clientId = 'apiComponentSite';
    var eventualDestination = window.location.href;
    var redirectUrl = window.location.protocol + "//" + window.location.host +
      wdk.webappUrl('/processLogin.do?redirectUrl=' +
        encodeURIComponent(eventualDestination));
    var oauthUrl = oauthServerBase + "/authorize?" +
        "response_type=code&" +
        "client_id=" + clientId + "&" +
        "redirect_uri=" + encodeURIComponent(redirectUrl);
    
    window.location = oauthUrl;
    return;

    // create div to house iframe and append generated iframe element
    var iframe = document.createElement('iframe');
    iframe.src = oauthUrl;
    jQuery(iframe).dialog({
        modal: true,
        closeOnEscape: false,
        open: function(event, ui) {
            jQuery(event.target).parent().find('.ui-dialog-titlebar-close').hide();
        },
        width: 'auto',
        title: 'Login...'
    });

    // get a handle on the <iframe>d document (in a cross-browser way)
    //var doc = iframe.contentWindow || iframe.contentDocument;
    //if (doc.document) {
    //  doc = doc.document;
    //}

    // open, write content to, and close the document
    //doc.open();
    //doc.write(frameContents);
    //doc.close();
  };

  ns.processLogin = function(submitButton) {
    $(submitButton).closest("form").submit();
  };

  ns.cancelLogin = function(cancelButton) {
    ns._dialog.dialog('close');
  };

  ns.logout = function() {
    if (confirm("Do you want to log out as " + ns.name() + "?")) {
      $("#user-control form[name=logoutForm]").submit();
    }
  };

  ns.validateRegistrationForm = function(e) {
      if (typeof e != 'undefined' && !enter_key_trap(e)) {
          return;
      }

      var email = document.registerForm.email.value;
      var pat = email.indexOf('@');
      var pdot = email.lastIndexOf('.');
      var len = email.length;

      if (email === '') {
          alert('Please provide your email address.');
          document.registerForm.email.focus();
          return false;
      } else if (pat<=0 || pdot<pat || pat==len-1 || pdot==len-1) {
          alert('The format of the email is invalid.');
          document.registerForm.email.focus();
          return false;
      } else if (email != document.registerForm.confirmEmail.value) {
          alert('The emails do not match. Please enter it again.');
          document.registerForm.email.focus();
          return false;
      } else if (document.registerForm.firstName.value === "") {
          alert('Please provide your first name.');
          document.registerForm.firstName.focus();
          return false;
      } else if (document.registerForm.lastName.value === "") {
          alert('Please provide your last name.');
          document.registerForm.lastName.focus();
          return false;
      } else if (document.registerForm.organization.value === "") {
          alert('Please provide the name of the organization you belong to.');
          document.registerForm.organization.focus();
          return false;
      } else {
          document.registerForm.registerButton.disabled = true;
          document.registerForm.submit();
          return true;
      }
  };

  ns.validateProfileForm = function(e) {
    if (typeof e != 'undefined' && !enter_key_trap(e)) {
      return;
      }
      if (document.profileForm.email.value != document.profileForm.confirmEmail.value) {
          alert("the email does not match.");
          document.profileForm.email.focus();
          return email;
      } else if (document.profileForm.firstName.value === "") {
          alert('Please provide your first name.');
          document.profileForm.firstName.focus();
          return false;
      } else if (document.profileForm.lastName.value === "") {
          alert('Please provide your last name.');
          document.profileForm.lastName.focus();
          return false;
      } else if (document.profileForm.organization.value === "") {
          alert('Please provide the name of the organization you belong to.');
          document.profileForm.organization.focus();
          return false;
      } else {
          document.profileForm.submit();
          return true;
      }
  };

  ns.validatePasswordFields = function(e) {
      if (typeof e != 'undefined' && !enter_key_trap(e)) {
          return false;
      }
      var newPassword = document.passwordForm.newPassword.value;
      var confirmPassword = document.passwordForm.confirmPassword.value;
      if (newPassword === "") {
          alert('The new password cannot be empty.');
          document.passwordForm.newPassword.focus();
          return false;
      } else if (newPassword != confirmPassword) {
          alert('The confirm password does not match with the new password.\nPlease verify your input.');
          document.passwordForm.newPassword.focus();
          return false;
      } else {
          document.passwordForm.changeButton.disabled = true;
          document.passwordForm.submit();
          return true;
      }
  };


  // User preferences
  // ----------------

  // Preference store - can be localStorage OR cookie
  var hasLocalStorage = Boolean(window.localStorage);
  var sessionId = $.cookie('JSESSIONID');

  // Returns the value for key, null if it doesn't exist
  ns.getPreference = function getPreference(key) {
    if (hasLocalStorage) {
      var item = JSON.parse(localStorage.getItem(key));
      if (item) {
        if (!item.session || item.session === sessionId) {
          return item.value;
        }
      }
      return null;
    }
    return $.cookie(key);
  };

  // Set the value of a preference.
  // Defaults to indefinite storage (1 year is using cookie fallback).
  //
  // - key: {String} Preference name
  // - value: {Any} Preference value
  // - session [optional]: {Boolean} Is preferenec session-only
  ns.setPreference = function setPreference(key, value, session) {
    if (hasLocalStorage) {
      localStorage.setItem(key, JSON.stringify({
        value: value,
        session: !!session && sessionId
      }));
    }
    else {
      $.cookie(key, value, session ? undefined : { expires: 365 });
    }
    return value;
  };

});
