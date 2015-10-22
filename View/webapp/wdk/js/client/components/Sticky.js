import React from 'react';
import { IntervalList } from '../utils/timerUtils';

let Sticky = React.createClass({

  statics: {
    intervalList: new IntervalList()
  },

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
    this.node = React.findDOMNode(this);
    this.$node = $(this.node);
    this.contentNode = React.findDOMNode(this.refs.content);
    Sticky.intervalList.add(this.updateIsFixed);
  },

  componentWillUnmount() {
    Sticky.intervalList.remove(this.updateIsFixed);
  },

  // Set position to fixed if top is above threshold, otherwise
  // set position to absolute.
  updateIsFixed() {
    requestAnimationFrame(() => {
      // See https://developer.mozilla.org/en-US/docs/Web/API/Element/getBoundingClientRect
      let offsetParent = this.node.offsetParent || document.body;
      let rect = this.node.getBoundingClientRect();
      let parentRect = offsetParent.getBoundingClientRect();
      let contentRect = this.contentNode.getBoundingClientRect();
      let top = rect.top - parentRect.top;
      if (top < 0 && this.state.isFixed === false) {
        this.setState({
          isFixed: true,
          height: rect.height,
          width: contentRect.width,
          top: parentRect.top
        });
      }
      else if (top >= 0 && this.state.isFixed === true) {
        this.setState({
          isFixed: false,
          height: null,
          width: null,
          top: ''
        });
      }
    });
  },

  render() {
    let { isFixed, height, width, top } = this.state;
    let { className, fixedClassName } = this.props;
    let style = Object.assign({}, this.props.style, {
      position: isFixed ? 'fixed' : '',
      top,
      width
    });
    if (isFixed) {
      className = className + ' ' + fixedClassName;
    }
    return (
      // This node is used to track scroll position
      <div style={{ height }}>
        <div ref="content" {...this.props} style={style} className={className}>
          {this.props.children}
        </div>
      </div>
    );
  }

});

export default Sticky;
