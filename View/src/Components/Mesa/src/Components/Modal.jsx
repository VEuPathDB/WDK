import React from 'react';

import { makeClassifier } from '../Utils/Utils';

const modalClass = makeClassifier('Modal');

class Modal extends React.Component {
  constructor (props) {
    super(props);
    this.componentDidMount = this.componentDidMount.bind(this);
  }

  componentDidMount () {
    
  }

  render () {
    return (
      <div className={modalClass()}>
        Modal!
      </div>
    );
  }
};

export default Modal;
