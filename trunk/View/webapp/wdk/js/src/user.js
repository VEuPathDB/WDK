wdk.util.namespace("window.wdk.user", function(ns, $) {
  "use strict";

  var userData;

  // init function
  ns.init = function() {
    userData = $("#wdk-user").data();
  };

  ns.id = function() { return userData.id; };
  ns.name = function() { return userData.name; };
  ns.country = function() { return userData.country; };
  ns.email = function() { return userData.email; };
  ns.isGuest = function() { return userData.isGuest; };
  ns.isUserLoggedIn = function() { return !ns.isGuest(); }

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
    var dialogHtml = $("#user-login-form").html();
    ns._dialog = $(dialogHtml).dialog({
      modal: true,
      close: function() { ns._dialog = null; } 
    }).find("input[name='redirectUrl']").val(redirectUrl);
  };

  ns.processLogin = function(submitButton) {
    $(submitButton).closest("form").submit();
  };

  ns.cancelLogin = function(cancelButton) {
    ns._dialog.dialog('close');
  };

  ns.logout = function() {
    if (confirm("Do you want to logout as " + ns.name() + "?")) {
      $("#user-control form[name=logoutForm]").submit();
    }
  };

  // replaced above, just below ns.init
  // ns.isUserLoggedIn = function() {
  //   return ($('#login-status').data('logged-in'));
  // };

  ns.validateRegistrationForm = function(e) {
      if (typeof e != 'undefined' && !enter_key_trap(e)) {
          return;
      }

      var email = document.registerForm.email.value;
      var pat = email.indexOf('@');
      var pdot = email.lastIndexOf('.');
      var len = email.length;

      if (email == '') {
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
      } else if (document.registerForm.firstName.value == "") {
          alert('Please provide your first name.');
          document.registerForm.firstName.focus();
          return false;
      } else if (document.registerForm.lastName.value == "") {
          alert('Please provide your last name.');
          document.registerForm.lastName.focus();
          return false;
      } else if (document.registerForm.organization.value == "") {
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
      } else if (document.profileForm.firstName.value == "") {
          alert('Please provide your first name.');
          document.profileForm.firstName.focus();
          return false;
      } else if (document.profileForm.lastName.value == "") {
          alert('Please provide your last name.');
          document.profileForm.lastName.focus();
          return false;
      } else if (document.profileForm.organization.value == "") {
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
      if (newPassword == "") {
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
  }

});
