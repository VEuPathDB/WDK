import URL from 'url';
import $ from 'jquery';
import { memoize } from 'lodash';
import { createElement } from 'react';
import { render } from 'react-dom';
import { alert } from './Platform';
import LoginForm from '../components/LoginForm';

type Config = {
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
  let { webappUrl } = config;
  if (config.method === 'OAUTH2') {
    let { oauthClientId, oauthUrl, webappUrl, serviceUrl } = config;
    let oauthTokenUrl = URL.resolve(location.origin, serviceUrl + '/oauth/stateToken');
    let redirectUrlBase = URL.resolve(location.origin, webappUrl + '/processLogin.do');

    $.getJSON(oauthTokenUrl).then((response: StateTokenResponse) => {
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
    }).fail(error => {
      alert("Unable to fetch your WDK state token.", "Please check your internet connection.");
      throw error;
    });
  }
  else {
    renderLoginForm(destination, webappUrl, true);
  }
}

export function logout(config: Config): void {
  let logoutUrl = URL.resolve(location.origin, config.webappUrl + '/processLogout.do');
  if (config.method === 'OAUTH2') {
    let { oauthUrl } = config;
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

function renderLoginForm(destination: string, webappUrl: string, open: boolean) {
  render(createElement('div', {}, createElement(LoginForm, {
    onCancel: () => {
      renderLoginForm(null, webappUrl, false);
    },
    onSubmit: () => {
    },
    open: open,
    action: webappUrl + '/processLogin.do',
    redirectUrl: destination,
    passwordResetUrl: webappUrl + '/showResetPassword.do',
    registerUrl: webappUrl + '/showRegister.do'
  })), getLoginContainer());
}
