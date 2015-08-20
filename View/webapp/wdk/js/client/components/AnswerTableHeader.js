import React from 'react';
import wrappable from '../utils/wrappable';

let AnswerTableHeader = React.createClass({
  propTypes: {
    descriptor: React.PropTypes.object.isRequired
  },

  render() {
    let { descriptor: { help, displayName } } = this.props;
    return <span title={help || ''}>{displayName}</span>;
  }
});

export default wrappable(AnswerTableHeader);
