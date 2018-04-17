import React from 'react';
import PropTypes from 'prop-types';

import { makeClassifier } from '../Utils/Utils';

const modalClass = makeClassifier('Modal');

// TODO: Delete or rewrite me.

class Modal extends React.Component {
  render () {
    return null;
  }
};

Modal.contextTypes = {
  addModal: PropTypes.func,
  removeModal: PropTypes.func
};

export default Modal;
