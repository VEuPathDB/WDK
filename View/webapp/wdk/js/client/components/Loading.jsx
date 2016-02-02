/* global Spinner */
import React from 'react';
import ReactDOM from 'react-dom';
import { wrappable } from '../utils/componentUtils';

/**
 * See http://fgnass.github.io/spin.js/
 */
let Loading = React.createClass({

  PropTypes: {
    className: React.PropTypes.string,
    radius: React.PropTypes.number
  },

  getDefaultProps() {
    return {
      className: '',
      radius: 8
    };
  },

  componentDidMount() {
    let { radius } = this.props;

    let opts = {
      lines: 11, // The number of lines to draw
      length: 3, // The length of each line
      width: 2, // The line thickness
      radius: radius, // The radius of the inner circle
      corners: 1, // Corner roundness (0..1)
      rotate: 0, // The rotation offset
      direction: 1, // 1: clockwise, -1: counterclockwise
      color: '#000', // #rgb or #rrggbb or array of colors
      speed: 1, // Rounds per second
      trail: 100, // Afterglow percentage
      shadow: false, // Whether to render a shadow
      hwaccel: false, // Whether to use hardware acceleration
      className: 'spinner', // The CSS class to assign to the spinner
      zIndex: 2e9, // The z-index (defaults to 2000000000)
      top: '50%', // Top position relative to parent
      left: '50%' // Left position relative to parent
    };

    new Spinner(opts).spin(ReactDOM.findDOMNode(this));
  },

  render() {
    let { className } = this.props;
    return (
      <div className={`wdk-Loading ${className}`}/>
    );
  }
});

export default wrappable(Loading);
