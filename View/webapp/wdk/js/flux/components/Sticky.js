import React from 'react';

let Sticky = React.createClass({

  getInitialState() {
    return { isFixed: false };
  },

  componentDidMount() {
    this.node = React.findDOMNode(this);
    window.addEventListener('scroll', this.updateIsFixed);
  },

  componentWillUnmount() {
    window.removeEventListener('scroll', this.updateIsFixed);
  },

  // Set position to fixed if top is above threshold, otherwise
  // set position to absolute.
  updateIsFixed() {
    // See https://developer.mozilla.org/en-US/docs/Web/API/Element/getBoundingClientRect
    let rect = this.node.getBoundingClientRect();
    if (rect.top < 0 && this.state.isFixed === false) {
      this.setState({
        isFixed: true
      });
    }
    else if (rect.top >= 0 && this.state.isFixed === true) {
      this.setState({
        isFixed: false
      });
    }
  },

  render() {
    let { isFixed } = this.state;
    let style = {
      position: isFixed ? 'fixed' : '',
      top: isFixed ? 0 : ''
    };
    return (
      <div> {/* This node is used to track scroll position */}
        <div style={style}>
          {this.props.children}
        </div>
      </div>
    );
  }

});

export default Sticky;
