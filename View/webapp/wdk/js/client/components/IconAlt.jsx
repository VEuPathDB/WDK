import React from 'react';

class Icon extends React.Component {
  constructor (props) {
    super(props);
  }

  render () {
    let { className, fa, onClick } = this.props;
    className = `fa fa-${fa} ${className || ''}`;
    let clickHandler = (onClick ? onClick : () => null);
    return (
      <i className={className} onClick={onClick}> </i>
    );
  }
};

export default Icon;
