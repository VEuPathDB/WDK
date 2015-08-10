import React from 'react';
import wrappable from '../utils/wrappable';

let AnswerTableHeader = React.createClass({
  propTypes: {
    attributeMeta: React.PropTypes.object.isRequired
  },

  render() {
    let { attributeMeta: { help, displayName } } = this.props;
    return <span title={help || ''}>{displayName}</span>;
  }
});

export default wrappable(AnswerTableHeader);
