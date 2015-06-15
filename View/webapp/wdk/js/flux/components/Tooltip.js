import React, { PropTypes } from 'react';

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

    if (content == null) return;

    let text = typeof content === 'string'
      ? content
      : React.renderToStaticMarkup(content);

    $(this.getDOMNode()).wdkTooltip({
      content: { text },
      show: { delay: 1000 },
      position: { my: 'top left', at: 'bottom left' }
    });
  },

  _destroyTooltip() {
    $(this.getDOMNode()).qtip('destroy');
  },

  render() {
    return React.Children.only(this.props.children);
  }

});

export default Tooltip;
