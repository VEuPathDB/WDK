import URL from 'url';
import $ from 'jquery';
import { memoize } from 'lodash';
import { createElement } from 'react';
import { render } from 'react-dom';
import { alert } from './Platform';
import LoginForm from '../components/LoginForm';
import WdkService from './WdkService';

export type Config = {
  webappUrl: string;
  serviceUrl: string;
  method: 'OAUTH2' | 'USERDB';
  oauthUrl: string;
  oauthClientId: string;
};

type StateTokenResponse = {
  oauthStateToken: string;
};

export function login(config: Config, destination: string): void {
  let { webappUrl, serviceUrl, oauthClientId, oauthUrl } = config;
  let wdkService = WdkService.getInstance(serviceUrl);
  if (config.method === 'OAUTH2') {
    performOAuthLogin(destination, webappUrl, wdkService, oauthClientId, oauthUrl);
  }
  else { // USER_DB
    renderLoginForm(destination, webappUrl, wdkService, true);
  }
}

export function logout(config: Config): void {
  let { method, oauthUrl, serviceUrl } = config;
  let logoutUrl = WdkService.getInstance(serviceUrl).getLogoutServiceEndpoint();
  if (config.method === 'OAUTH2') {
    let googleSpecific = (oauthUrl.indexOf("google") != -1);
    // don't log user out of google, only the eupath oauth server
    let nextPage = (googleSpecific ? logoutUrl :
      oauthUrl + "/logout?redirect_uri=" + encodeURIComponent(logoutUrl));
    window.location.assign(nextPage);
  }
  else {
    window.location.assign(logoutUrl);
  }
}

function performOAuthLogin(destination: string, webappUrl: string,
    wdkService: WdkService, oauthClientId: string, oauthUrl: string) {
  wdkService.getOauthStateToken()
    .then((response: StateTokenResponse) => {
      let redirectUrlBase = wdkService.getLoginServiceEndpoint();
      let googleSpecific = (oauthUrl.indexOf("google") != -1);
      let [ redirectUrl, authEndpoint ] = googleSpecific ?
        [ redirectUrlBase, "auth" ] : // hacks to conform to google OAuth2 API
        [ redirectUrlBase + '?redirectUrl=' + encodeURIComponent(destination), "authorize" ];

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

function renderLoginForm(destination: string, webappUrl: string, wdkService: WdkService, open: boolean, message?: string) {
  render(createElement('div', {}, createElement(LoginForm, {
    onCancel: () => {
      renderLoginForm(destination, webappUrl, wdkService, false);
    },
    onSubmit: (email: string, password: string) => {
      wdkService.tryLogin(email, password, destination)
        .then(response => {
          if (response.success) {
            window.location.assign(response.redirectUrl);
          }
          else {
            renderLoginForm(destination, webappUrl, wdkService, true, response.message);
          }
        })
        .catch(error => {
          alert("Error", "There was an error submitting your credentials.  Please try again later.");
        });
    },
    open: open,
    message: message,
    passwordResetUrl: webappUrl + '/app/user/forgot-password',
    registerUrl: webappUrl + '/app/user/registration'
  })), getLoginContainer());
}
