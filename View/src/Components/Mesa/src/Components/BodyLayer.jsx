import React from 'react';
import ReactDOM from 'react-dom';

import { uid } from '../Utils/Utils';

class BodyLayer extends React.Component {
  constructor (props) {
    super(props);
    this.id = '_BodyLayer_' + uid();
    this.parentElement = null;
  }

  render () {
    return null;
  }

  componentDidMount () {
    let element = document.getElementById(this.id);
    if (!element) {
      element = document.createElement('div');
      element.id = this.id;
      document.body.appendChild(element);
    }
    this.parentElement = element;
    this.componentDidUpdate();
  }

  componentWillUnmount () {
    document.body.removeChild(this.parentElement);
  }

  componentDidUpdate () {
    const { props } = this;
    ReactDOM.render(<div {...props} />, this.parentElement);
  }
};

export default BodyLayer;
