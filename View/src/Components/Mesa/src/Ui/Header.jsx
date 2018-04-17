import React from 'react';

class Header extends React.Component {
  constructor (props) {
    super(props);
  }

  render () {
    return (
      <header>
        <img src="logo.png" className="Logo" />
      </header>
    );
  }
};

export default Header;
