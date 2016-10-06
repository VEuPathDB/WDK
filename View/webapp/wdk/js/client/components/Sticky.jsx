import React from 'react';
import ReactDOM from 'react-dom';

let Sticky = React.createClass({

  propTypes: {
    className: React.PropTypes.string,
    fixedClassName: React.PropTypes.string
  },

  getDefaultProps() {
    return {
      className: 'wdk-Sticky',
      fixedClassName: 'wdk-Sticky-fixed'
    };
  },

  getInitialState() {
    return { isFixed: false, height: null, width: null };
  },

  componentDidMount() {
    this.node = ReactDOM.findDOMNode(this);
    this.$node = $(this.node);
    this.contentNode = ReactDOM.findDOMNode(this.refs.content);
    window.addEventListener('scroll', this.updateIsFixed, { passive: true });
    window.addEventListener('wheel', this.updateIsFixed, { passive: true });
    window.addEventListener('resize', this.updateIsFixed, { passive: true });
  },

  componentWillUnmount() {
    window.removeEventListener('scroll', this.updateIsFixed, { passive: true });
    window.removeEventListener('wheel', this.updateIsFixed, { passive: true });
    window.removeEventListener('resize', this.updateIsFixed, { passive: true });
  },

  // Set position to fixed if top is above threshold, otherwise
  // set position to absolute.
  updateIsFixed() {
    // See https://developer.mozilla.org/en-US/docs/Web/API/Element/getBoundingClientRect
    let rect = this.node.getBoundingClientRect();
    if (rect.top < 0 && this.state.isFixed === false) {
      let contentRect = this.contentNode.getBoundingClientRect();
      this.setState({
        isFixed: true,
        height: rect.height,
        width: contentRect.width
      });
    }
    else if (rect.top >= 0 && this.state.isFixed === true) {
      this.setState({
        isFixed: false,
        height: null,
        width: null
      });
    }
  },

  render() {
    let { isFixed, height, width, top } = this.state;
    let { className, fixedClassName } = this.props;
    if (isFixed) {
      className = className + ' ' + fixedClassName;
    }
    return (
      // This node is used to track scroll position
      <div style={{ height }}>
        <div ref="content" {...this.props} className={className}>
          {this.props.children}
        </div>
      </div>
    );
  }

});

export default Sticky;
