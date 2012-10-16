var User = function() {
	// can define "instance" methods here
};

// var to hold reference to login dialog
User._dialog = null;

User.populateUserControl = function(userData) {
    // populate data field
	var loggedInValue = (userData.isLoggedIn ? "true" : "false");
    jQuery('#login-status').attr('data-logged-in', loggedInValue);
	
	// populate visible section
	var templateSelector = (userData.isLoggedIn ?
	    "#user-logged-in" : "#user-not-logged-in");
	var html = jQuery(templateSelector).html();
	var template = Handlebars.compile(html);
	var html2 = template(userData);
	jQuery('#user-control').html(html2);
};

User.login = function(redirectUrl) {
	var dialogHtml = jQuery("#user-login-form").html();
	User._dialog = jQuery(dialogHtml).dialog({
		modal: true,
		close: function() { User._dialog = null; } 
	}).find("input[name='redirectUrl']").val(redirectUrl);
};

User.processLogin = function(submitButton) {
	jQuery(submitButton).closest("form").submit();
};

User.cancelLogin = function(cancelButton) {
	User._dialog.dialog('close');
};

User.logout = function() {
	var userName = jQuery("#user-control #user-name").text();
	if (confirm("Do you want to logout as " + userName + "?")) {
		jQuery("#user-control form[name=logoutForm]").submit();
	}
};

User.isUserLoggedIn = function() {
  return (jQuery('#login-status').attr('data-logged-in') == 'true');
};

User.validateRegistrationForm = function(e) {
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

User.validateProfileForm = function(e) {
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

User.validatePasswordFields = function(e) {
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
