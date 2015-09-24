import React from 'react';
import { wrappable } from '../utils/componentUtils';

let Main = React.createClass({
  propTypes: {
    withSidebar: React.PropTypes.bool
  },

  getDefaultProps() {
    return {
      withSidebar: false,
      className: ''
    };
  },

  getInitialState() {
    return {
      sidebarVisible: this.props.withSidebar
    };
  },

  toggleVisible(e) {
    e.preventDefault();
    this.setState({ sidebarVisible: !this.state.sidebarVisible });
  },

  render() {
    let { sidebarVisible } = this.state;
    let classes = this.props.className + ' wdk-PageColumn ' + ( sidebarVisible
      ? 'wdk-PageColumn-main-with-sidebar'
      : 'wdk-PageColumn-main');
    return (
      <div className={classes}>
        {this.props.withSidebar
          ? <a href="#"
              title={(sidebarVisible ? 'Hide' : 'Show') + ' panel'}
              className="wdk-SidebarToggle"
              onClick={this.toggleVisible}
            >
              <i className="fa fa-lg fa-angle-double-left"></i>
            </a>
          : null}
        {this.props.children}
      </div>
    );
  }
});

export default wrappable(Main);
