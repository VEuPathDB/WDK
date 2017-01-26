/**
 * Wrapper for the jquery plugin q-tip (http://qtip2.com/).
 *
 * This will eventually be replaced by a pure React component, as a part of an
 * initiative to remove our jQuery dependency.
 */
import $ from 'jquery';
import React, { PropTypes } from 'react';
import ReactDOM from 'react-dom';
import ReactDOMServer from 'react-dom/server';
import { wrappable } from '../utils/componentUtils';

let defaultOptions = {
  position: {
    my: "top left",
    at: "bottom left"
  },
  hide: {
    fixed: true,
    delay: 250
  },
  show: {
    solo: true,
    delay: 1000
  },
  style: {
    classes: 'qtip-tipsy'
  }
};

type Props = {
  content: string | React.ReactElement<any>;
}

class Tooltip extends React.Component<Props, void> {

  componentDidMount() {
    this._setupTooltip();
  }

  componentDidUpdate() {
    this._destroyTooltip();
    this._setupTooltip();
  }

  componentWillUnmount() {
    this._destroyTooltip();
  }

  _setupTooltip() {
    let { content } = this.props;

    if (content == null) { return; }

    let text = typeof content === 'string'
      ? content
      : ReactDOMServer.renderToStaticMarkup(content);

    $(ReactDOM.findDOMNode(this)).qtip(Object.assign({
      content: { text }
    }, defaultOptions));
  }

  _destroyTooltip() {
    $(ReactDOM.findDOMNode(this)).qtip('destroy');
  }

  render() {
    return React.Children.only(this.props.children);
  }

}

export default wrappable(Tooltip);
