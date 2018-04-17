import React from 'react';

class Header extends React.Component {
  constructor (props) {
    super(props);
  }

  render () {
    return (
      <div className="Header">
        <grid>
          <box>
            <h1>WDK Table Demo</h1>
          </box>
          <box className="attribution">
            Mesa <small>By</small> Austin
          </box>
        </grid>
      </div>
    );
  }
};

export default Header;
