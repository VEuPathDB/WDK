import URL from 'url';
import $ from 'jquery';
import { memoize } from 'lodash';
import { createElement } from 'react';
import { render } from 'react-dom';
import { alert } from 'Utils/Platform';
import LoginForm from 'Views/User/LoginForm';
import WdkService from 'Utils/WdkService';

export function login(wdkService: WdkService, destination: string): void {
  wdkService.getConfig().then(config => {
    config.authentication.method
    let { oauthClientId, oauthClientUrl, oauthUrl, method } = config.authentication;
    if (method === 'OAUTH2') {
      performOAuthLogin(destination, wdkService, oauthClientId, oauthClientUrl, oauthUrl);
    }
    else { // USER_DB
      renderLoginForm(destination, wdkService, true);
    }
  });
}

export function logout(wdkService: WdkService): void {
  wdkService.getConfig().then(config => {
    let { oauthClientId, oauthClientUrl, oauthUrl, method } = config.authentication;
    let logoutUrl = oauthClientUrl + '/logout';
    if (method === 'OAUTH2') {
      let googleSpecific = (oauthUrl.indexOf("google") != -1);
      // don't log user out of google, only the eupath oauth server
      let nextPage = (googleSpecific ? logoutUrl :
        oauthUrl + "/logout?redirect_uri=" + encodeURIComponent(logoutUrl));
      window.location.assign(nextPage);
    }
    else {
      window.location.assign(logoutUrl);
    }
  });
}

function performOAuthLogin(destination: string, wdkService: WdkService,
  oauthClientId: string, oauthClientUrl: string, oauthUrl: string) {
  wdkService.getOauthStateToken()
    .then((response) => {
      let googleSpecific = (oauthUrl.indexOf("google") != -1);
      let [ redirectUrl, authEndpoint ] = googleSpecific ?
        [ oauthClientUrl + '/login', "auth" ] : // hacks to conform to google OAuth2 API
        [ oauthClientUrl + '/login?redirectUrl=' + encodeURIComponent(destination), "authorize" ];

      let finalOauthUrl = oauthUrl + "/" + authEndpoint + "?" +
        "response_type=code&" +
        "scope=" + encodeURIComponent("openid email") + "&" +
        "state=" + encodeURIComponent(response.oauthStateToken) + "&" +
        "client_id=" + oauthClientId + "&" +
        "redirect_uri=" + encodeURIComponent(redirectUrl);

      window.location.assign(finalOauthUrl);
    })
    .catch(error => {
      alert("Unable to fetch your WDK state token.", "Please check your internet connection.");
      throw error;
    });
}

let getLoginContainer = memoize(function() {
  return <HTMLElement>document.body.appendChild(document.createElement('div'));
});

function renderLoginForm(destination: string, wdkService: WdkService, open: boolean, message?: string) {
  render(createElement('div', {}, createElement(LoginForm, {
    onCancel: () => {
      renderLoginForm(destination, wdkService, false);
    },
    onSubmit: (email: string, password: string) => {
      wdkService.tryLogin(email, password, destination)
        .then(response => {
          if (response.success) {
            window.location.assign(response.redirectUrl);
          }
          else {
            renderLoginForm(destination, wdkService, true, response.message);
          }
        })
        .catch(error => {
          alert("Error", "There was an error submitting your credentials.  Please try again later.");
        });
    },
    open: open,
    message: message,
    passwordResetPath: '/user/forgot-password',
    registerPath: '/user/registration'
  })), getLoginContainer());
}
