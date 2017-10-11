import React from 'react';
import { wrappable } from '../utils/componentUtils';
import Dialog from './Dialog';

type Props = {
  onCancel: () => void;
  onSubmit: () => void;
  open: boolean;
  action: string;
  redirectUrl: string;
  passwordResetUrl: string;
  registerUrl: string;
};
/**
 * Form used for authorizing against webapp instead of oauth server.
 */
function LoginForm(props: Props) {
  return props.open === false ? <noscript/> : (
    <Dialog title="Login" open={true} modal={true} onClose={props.onCancel}>
      <form onSubmit={props.onSubmit} name="loginForm" method="post" action={props.action}>
        <input value={props.redirectUrl} name="redirectUrl" type="hidden"/>
        <table>
          <tbody>
            <tr>
              <td style={{ textAlign: 'right' }}>
                <div className="small">
                  <b>Email: </b>
                </div>
              </td>
              <td style={{ textAlign: 'left' }}>
                <div className="small">
                  <input size={20} name="email" type="text" id="email"/>
                </div>
              </td>
            </tr>
            <tr>
              <td style={{ textAlign: 'right' }}>
                <div className="small">
                  <b>Password: </b>
                </div>
              </td>
              <td style={{ textAlign: 'left' }}>
                <div className="small">
                  <input size={20} name="password" type="password" id="password"/>
                </div>
              </td>
            </tr>
            <tr>
              <td style={{ textAlign: 'center', whiteSpace: 'nowrap' }} colSpan={2}>
                <input size={11} name="remember" id="remember" type="checkbox"/> Remember me on this computer.
              </td>
            </tr>
            <tr>
              <td style={{ textAlign: 'center', whiteSpace: 'nowrap' }} colSpan={2}>
                <span className="small">
                  <input style={{width:76, height:30, fontSize: '1em'}} id="login" value="Login" type="submit"/>
                  <input onClick={props.onCancel} style={{width:76, height:30, fontSize: '1em'}} value="Cancel" type="button"/>
                </span>
              </td>
            </tr>
            <tr>
              <td style={{ textAlign: 'center', verticalAlign: 'top' }} colSpan={2}>
                <span className="small">
                  <a href={props.passwordResetUrl} style={{paddingRight:15 }}>Forgot Password?</a>
                  <a href={props.registerUrl}>Register/Subscribe</a>
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </form>
    </Dialog>
  );
}

export default wrappable(LoginForm);
