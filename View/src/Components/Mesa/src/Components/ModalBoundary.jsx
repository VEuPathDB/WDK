import React from 'react';
import PropTypes from 'prop-types';

import { makeClassifier } from '../Utils/Utils';

const modalBoundaryClass = makeClassifier('ModalBoundary');

class ModalBoundary extends React.Component {
  constructor (props) {
    super(props);

    this.state = {
      modals: []
    };

    this.getChildContext = this.getChildContext.bind(this);
    this.addModal = this.addModal.bind(this);
    this.removeModal = this.removeModal.bind(this);
  }

  addModal (modal) {
    let { modals } = this.state;
    if (modals.indexOf(modal) < 0) modals.push(modal);
    this.setState({ modals });
  }

  removeModal (modal) {
    let { modals } = this.state;
    let index = modals.indexOf(modal)
    if (index < 0) return;
    modals.splice(index, 1);
    this.setState({ modals });
  }

  getChildContext () {
    const { addModal, removeModal } = this;
    return { addModal, removeModal };
  }

  getBoundaryStyle () {
    return {
      width: '100%',
      height: '100%',
      display: 'block',
      position: 'relative',
      border: '10px solid orange'
    };
  }

  renderModalList () {
    const { modals } = this.state;
    return (
      <div>Lol</div>
    );
  }

  getWrapperStyle () {
    return {
      position: 'fixed',
      top: 0,
      left: 0,
      width: '100vw',
      height: '100vh',
      backgroundColor: 'rgba(0,0,0,0.4)',
      border: '2px solid orange'
    }
  }

  render () {
    const { children } = this.props;
    const ModalList = this.renderModalList;
    const style = this.getBoundaryStyle();

    return (
      <div className={modalBoundaryClass()} style={style}>
        {children}
        <div style={this.getWrapperStyle()}>
          Modals:
          <ModalList />
        </div>
      </div>
    );
  }
};

ModalBoundary.childContextTypes = {
  addModal: PropTypes.func,
  removeModal: PropTypes.func
};

export default ModalBoundary;
