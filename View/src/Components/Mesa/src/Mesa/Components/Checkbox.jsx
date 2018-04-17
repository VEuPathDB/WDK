import React from 'react';

import Icon from 'Mesa/Components/Icon';

class Checkbox extends React.Component {
  constructor (props) {
    super(props);
    this.handleClick = this.handleClick.bind(this);
  }

  handleClick (e) {
    let { checked, onChange } = this.props;
    if (typeof onChange === 'function') onChange(!!checked);
  }

  render () {
    let { checked, className } = this.props;
    className = 'Checkbox' + (className ? ' ' + className : '');
    className += ' ' + (checked ? 'Checkbox-Checked' : 'Checkbox-Unchecked');

    return (
      <div className={className} onClick={this.handleClick}>
        <Icon fa={checked ? 'check-square' : 'square'} />
      </div>
    );
  }
};

export default Checkbox;
