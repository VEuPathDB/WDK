import React from 'react';

class Icon extends React.PureComponent {
  render () {
    let { fa, onClick, style } = this.props;
    let className = `icon fa fa-${fa}`;
    return <i onClick={onClick} style={style} className={className}> </i>
  }
};

export default Icon;