import * as React from 'react';
import { wrappable } from '../utils/componentUtils';
import NotFound from '../components/NotFound';
import WdkViewController from './WdkViewController';
import { conditionallyTransition } from '../actioncreators/UserActionCreators';

const ActionCreators = { conditionallyTransition };

class UserMessageController extends WdkViewController<typeof ActionCreators> {

  getMessagePageContent() {
    switch (this.props.match.params.messageKey) {
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

  loadData() {
    // if registered user is logged in, show profile instead of password reset message
    if (this.props.match.params.messageKey == 'password-reset-successful') {
      this.eventHandlers.conditionallyTransition(user => !user.isGuest, '/user/profile');
    }
  }

  getActionCreators() {
    return { conditionallyTransition };
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
