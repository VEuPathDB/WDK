import React from 'react';
import { showLoginForm } from 'Core/ActionCreators/UserActionCreators';

import Icon from 'Components/Icon/IconAlt';

class NotLoggedIn extends React.Component {
  constructor (props) {
    super(props);
  }

  render () {
    return (
      <div className="UserDatasetList-NotLoggedIn">
        <Icon fa="list-alt" />
        <p>Please <a onClick={() => showLoginForm()}>log in</a> to upload and view your user datasets.</p>
      </div>
    );
  }
};

export default NotLoggedIn;
