import React, { PropTypes } from 'react';

const Tooltip = React.createClass({

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
    const { content } = this.props;

    if (content == null) return;

    $(this.getDOMNode()).wdkTooltip({
      content: { text: React.renderToStaticMarkup(content) },
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
