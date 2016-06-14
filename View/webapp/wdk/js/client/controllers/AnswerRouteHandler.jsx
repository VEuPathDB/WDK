// Import modules
import React from 'react';
import AnswerContainer from './AnswerContainer';
import { wrappable } from '../utils/componentUtils';

/** Route handler */
function AnswerController(props) {
  return (
    <AnswerContainer
      dispatchAction={props.dispatchAction}
      stores={props.stores}
      questionName={props.params.question}
      recordClassName={props.params.recordClass}
      parameters={props.location.query}
    />
  );
}

export default wrappable(AnswerController);
