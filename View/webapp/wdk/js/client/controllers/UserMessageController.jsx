import { wrappable } from '../utils/componentUtils';
import NotFound from '../components/NotFound';
import WdkViewController from './WdkViewController';

class UserMessageController extends WdkViewController {

  getMessagePageContent() {
    switch (this.props.match.params.messageKey) {
      case 'registration-successful':
        return {
          tabTitle: "Registration Successful!",
          pageTitle: "Registration Successful!",
          pageContent: (<span>You will receive an email shortly containing a 
            temporary password.  Use your registration email to log in.</span>)
        };
      case 'password-reset-successful':
        return {
          tabTitle: "Password Reset",
          pageTitle: "Success!",
          pageContent: (<span>You will receive an email shortly containing a
            new, temporary password.</span>)
        };
      default:
        return {
          tabTitle: "Page Not Found",
          pageTitle: "",
          pageContent: (<NotFound/>)
        };
    }
  }

  getTitle() {
    return this.getMessagePageContent().tabTitle;
  }

  renderView() {
    let content = this.getMessagePageContent();
    return (
      <div>
        <h1>{content.pageTitle}</h1>
        {content.pageContent}
      </div>
    );
  }
}

export default wrappable(UserMessageController);
