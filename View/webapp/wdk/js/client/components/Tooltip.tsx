/**
 * Wrapper for the jquery plugin q-tip (http://qtip2.com/).
 *
 * This will eventually be replaced by a pure React component, as a part of an
 * initiative to remove our jQuery dependency.
 */
import $ from 'jquery';
import React from 'react';
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
  }
};

// FIXME Add `renderContent` props that is a function that returns `typeof content`
type Props = {
  content: string | React.ReactElement<any>;
  open?: boolean;
  classes?: string;
  showTip?: boolean;
  showEvent?: string;
  showDelay?: number;
  hideEvent?: string;
  hideDelay?: number;
  position?: {
    my?: string;
    at?: string;
  }
  solo?: boolean;
  children: React.ReactChild;
  onShow?: (e: Event) => void;
  onHide?: (e: Event) => void;
}

class Tooltip extends React.PureComponent<Props> {

  api?: QTip2.Api;

  contentContainer: HTMLDivElement;

  componentDidMount() {
    this._setupTooltip(this.props);
  }

  componentWillReceiveProps(nextProps: Props) {
    if (this.api == null) {
      this._setupTooltip(nextProps);
      return;
    }

    if (this.props.content !== nextProps.content) {
      this._setContent(nextProps.content);
    }

    if (nextProps.open != null) {
      this.api.toggle(nextProps.open);
    }

    if (nextProps.classes != null) {
      this.api.set('style.classes', nextProps.classes);
    }

    if (nextProps.showEvent != null) {
      this.api.set('show.event', nextProps.open == null ? nextProps.showEvent : false);
    }

    if (nextProps.hideEvent != null) {
      this.api.set('hide.event', nextProps.open == null ? nextProps.hideEvent : false);
    }

    if (nextProps.position != null) {
      this.api.set('position.at', nextProps.position.at);
      this.api.set('position.my', nextProps.position.my);
    }

    this.api.reposition();
  }

  componentWillUnmount() {
    this._destroyTooltip();
  }

  _setupTooltip(props: Props) {
    let {
      content,
      open,
      showEvent,
      showDelay = defaultOptions.show.delay,
      hideEvent,
      hideDelay = defaultOptions.hide.delay,
      classes = 'qtip-tipsy',
      position = defaultOptions.position,
      solo = true,
      showTip = true
    } = props;

    if (content == null) { return; }

    if (open != null && (showEvent != null || hideEvent != null)) {
      console.warn('Tooltip was passed props `open` and either `showEvent` or `hideEvent`. ' +
        'Since `open` was provided, `showEvent` and `hideEvent` will be ignored.');
    }

    this.contentContainer = document.createElement('div');

    this.api = $(ReactDOM.findDOMNode(this)).qtip({
      content: { text: $(this.contentContainer) },
      style: { classes, tip: { corner: showTip } },
      show: { ...defaultOptions.show, solo, event: open == null ? showEvent : false, delay: showDelay },
      hide: { ...defaultOptions.hide, event: open == null ? hideEvent : false, delay: hideDelay },
      position,
      events: {
        show: event => {
          if (props.onShow) props.onShow(event);
        },
        hide: event => {
          if (props.onHide) props.onHide(event);
        }
      }
    }).qtip('api');

    this._setContent(content);

    if (open != null) this.api.toggle(open);
  }

  _destroyTooltip() {
    $(ReactDOM.findDOMNode(this)).qtip('destroy');
  }

  _setContent(content: Props['content']) {
    if (typeof content === 'string') {
      $(this.contentContainer).empty().append(content);
    }
    else {
      ReactDOM.unstable_renderSubtreeIntoContainer(
        this,
        content,
        this.contentContainer
      );
    }
  }

  render() {
    return React.Children.only(this.props.children);
  }

}

export default wrappable(Tooltip);
