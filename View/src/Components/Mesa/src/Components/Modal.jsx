import React from 'react';
import PropTypes from 'prop-types';

import { makeClassifier } from '../Utils/Utils';

const modalClass = makeClassifier('Modal');

class Modal extends React.Component {
  constructor (props) {
    super(props);
    this.componentDidMount = this.componentDidMount.bind(this);
  }
  
  render () {
    return (
      <div className={modalClass()}>
        Modal!
      </div>
    );
  }
};

Modal.contextTypes = {
  addModal: PropTypes.func,
  removeModal: PropTypes.func
};

export default Modal;
