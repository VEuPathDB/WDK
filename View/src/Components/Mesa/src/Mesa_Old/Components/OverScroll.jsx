import React from 'react';

import Defaults from 'Mesa/Defaults';
import Utils from 'Mesa/Utils/Utils';

class OverScroll extends React.Component {
  constructor (props) {
    super(props);
  }

  render () {
    let { className, height } = this.props;
    className = 'OverScroll' + (className ? ' ' + className : '');
    height = typeof height === 'number' ? height + 'px' : Defaults.overflowHeight;

    const style = {
      maxHeight: height,
      overflowY: 'auto'
    };

    return (
      <div className={className}>
        <div className="OverScroll-Inner" style={style}>
          {this.props.children}
        </div>
      </div>
    );
  }
};

export default OverScroll;
