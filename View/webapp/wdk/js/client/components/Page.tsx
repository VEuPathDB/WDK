/**
 * Page wrapper used by view controllers.
 */
import React from 'react';
import {wrappable} from '../utils/componentUtils';
import Header from './Header';
import Footer from './Footer';
import LoginForm from './LoginForm';
import {GlobalData} from '../stores/GlobalDataStore';

type Props = {
  children: React.ReactChild;
  globalData: GlobalData;
  dismissLoginForm: Function;
};

function Page(props: Props) {
  let { config, user } = props.globalData;
  return (
    <div className="wdk-RootContainer">
      <Header {...props}/>
      {user && user.showLoginForm && (
        <LoginForm
          onCancel={props.dismissLoginForm}
          onSubmit={() => {}}
          open={true}
          action={config.webAppUrl + '/processLogin.do'}
          redirectUrl={user.destination}
          passwordResetUrl={config.webAppUrl + '/showResetPassword.do'}
          registerUrl={config.webAppUrl + '/showRegister.do'}
        />
      )}
      <div className="wdk-PageContent">{props.children}</div>
      <Footer {...props}/>
    </div>
  );
}

export default wrappable(Page);
