/**
 * Page wrapper used by view controllers.
 */
import React from 'react';
import {wrappable} from '../utils/componentUtils';
import Header from './Header';
import Footer from './Footer';
import LoginForm from './LoginForm';
import {User} from "../utils/WdkUser";
import {ServiceConfig} from "../utils/WdkService";

type Props = {
  children: React.ReactChild,
  user: User & {
    showLoginForm: boolean;
    destination: string;
  };
  dismissLoginForm: Function;
  config: ServiceConfig;
};

function Page(props: Props) {
  return (
    <div className="wdk-RootContainer">
      <Header {...props}/>
      {props.user && props.user.showLoginForm && (
        <LoginForm
          onCancel={props.dismissLoginForm}
          onSubmit={() => {}}
          open={true}
          action={props.config.webAppUrl + '/processLogin.do'}
          redirectUrl={props.user.destination}
          passwordResetUrl={props.config.webAppUrl + '/showResetPassword.do'}
          registerUrl={props.config.webAppUrl + '/showRegister.do'}
        />
      )}
      <div className="wdk-PageContent">{props.children}</div>
      <Footer {...props}/>
    </div>
  );
}

export default wrappable(Page);
