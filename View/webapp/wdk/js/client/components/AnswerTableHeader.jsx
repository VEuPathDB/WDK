import React from 'react';
import { wrappable } from '../utils/componentUtils';

function AnswerTableHeader(props) {
  let { descriptor: { help, displayName } } = props;
  return <span title={help || ''}>{displayName}</span>;
}

AnswerTableHeader.propTypes = {
  descriptor: React.PropTypes.object.isRequired
};


export default wrappable(AnswerTableHeader);
