import { Component, PropTypes } from 'react';

export default class WdkContext extends Component {

  constructor(props) {
    super(props);
    this.dispatchAction = props.dispatchAction;
    this.stores = props.stores;
  }

  getChildContext() {
    return {
      dispatchAction: this.props.dispatchAction,
      stores: this.props.stores
    };
  }

  render() {
    return this.props.children;
  }

}

WdkContext.propTypes = {
  dispatchAction: PropTypes.func.isRequired,
  stores: PropTypes.object.isRequired,
  children: PropTypes.element.isRequired
};

WdkContext.childContextTypes = {
  dispatchAction: PropTypes.func.isRequired,
  stores: PropTypes.object.isRequired
};
