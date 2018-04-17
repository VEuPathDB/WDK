import React from 'react';

import { makeClassifier } from '../Utils/Utils';

const modalBoundaryClass = makeClassifier('ModalBoundary');

class ModalBoundary extends React.Component {
  constructor (props) {
    super(props);
    this.componentDidMount = this.componentDidMount.bind(this);
  }

  componentDidMount () {

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

  render () {
    const { children } = this.props;
    const style = this.getBoundaryStyle();

    return (
      <div className={modalBoundaryClass()} style={style}>
        {children}
      </div>
    );
  }
};

export default ModalBoundary;
