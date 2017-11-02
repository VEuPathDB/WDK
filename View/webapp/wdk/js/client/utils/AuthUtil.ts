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
  let { webappUrl, serviceUrl } = config;
  if (config.method === 'OAUTH2') {
    let { oauthClientId, oauthUrl, webappUrl, serviceUrl } = config;
    let wdkService = WdkService.getInstance(serviceUrl);
    wdkService.getOauthStateToken().then((response: StateTokenResponse) => {
      //let redirectUrlBase = URL.resolve(location.origin, webappUrl + '/processLogin.do');
      let redirectUrlBase = wdkService.getLoginServiceEndpoint();
      let googleSpecific = (oauthUrl.indexOf("google") != -1);
      let redirectUrl: string, authEndpoint: string;
      if (googleSpecific) {
        // hacks to conform to google OAuth2 API
        redirectUrl = redirectUrlBase;
        authEndpoint = "auth";
      }
      else {
        redirectUrl = redirectUrlBase + '?redirectUrl=' + encodeURIComponent(destination);
        authEndpoint = "authorize";
      }

      let finalOauthUrl = oauthUrl + "/" + authEndpoint + "?" +
        "response_type=code&" +
        "scope=" + encodeURIComponent("openid email") + "&" +
        "state=" + encodeURIComponent(response.oauthStateToken) + "&" +
        "client_id=" + oauthClientId + "&" +
        "redirect_uri=" + encodeURIComponent(redirectUrl);

      window.location.assign(finalOauthUrl);
    }).catch(error => {
      alert("Unable to fetch your WDK state token.", "Please check your internet connection.");
      throw error;
    });
  }
  else {
    renderLoginForm(destination, webappUrl, serviceUrl, true);
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

let getLoginContainer = memoize(function() {
  return <HTMLElement>document.body.appendChild(document.createElement('div'));
});

function renderLoginForm(destination: string, webappUrl: string, serviceUrl: string, open: boolean) {
  let loginUrl = webappUrl + '/processLogin.do';
  //let loginUrl = WdkService.getInstance(serviceUrl).getLogoutServiceEndpoint();
  render(createElement('div', {}, createElement(LoginForm, {
    onCancel: () => {
      renderLoginForm(destination, webappUrl, serviceUrl, false);
    },
    onSubmit: () => {
    },
    open: open,
    action: loginUrl,
    redirectUrl: destination,
    passwordResetUrl: webappUrl + '/app/user/forgot-password',
    registerUrl: webappUrl + '/app/user/registration'
  })), getLoginContainer());
}
