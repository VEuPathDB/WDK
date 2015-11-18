import React, { PropTypes } from 'react';
import ReactDOM from 'react-dom';
import ReactDOMServer from 'react-dom/server';
import { wrappable } from '../utils/componentUtils';

let $ = window.jQuery;

let Tooltip = React.createClass({

  propTypes: {
    // string or ReactElement
    content: PropTypes.oneOfType([
      PropTypes.string,
      PropTypes.element
    ])
  },

  componentDidMount() {
    this._setupTooltip();
  },

  componentDidUpdate() {
    this._destroyTooltip();
    this._setupTooltip();
  },

  componentWillUnmount() {
    this._destroyTooltip();
  },

  _setupTooltip() {
    let { content } = this.props;

    if (content == null) { return; }

    let text = typeof content === 'string'
      ? content
      : ReactDOMServer.renderToStaticMarkup(content);

    $(ReactDOM.findDOMNode(this)).wdkTooltip({
      content: { text },
      show: { delay: 1000 },
      position: { my: 'top left', at: 'bottom left' }
    });
  },

  _destroyTooltip() {
    $(ReactDOM.findDOMNode(this)).qtip('destroy');
  },

  render() {
    return React.Children.only(this.props.children);
  }

});

export default wrappable(Tooltip);
