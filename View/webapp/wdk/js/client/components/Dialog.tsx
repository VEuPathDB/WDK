import React, {Component, Children, ReactChild} from 'react';
import {render, unmountComponentAtNode, findDOMNode} from 'react-dom';
import $ from 'jquery';
import { wrappable } from '../utils/componentUtils';

type Props = {
  open?: boolean;
  modal?: boolean;
  title?: string;
  onOpen?: Function;
  onClose?: Function;
  children?: React.ReactChild;
  width?: number | string;
  height?: number | string;
  draggable?: boolean;
  resizable?: boolean;
  className?: string;
};

/**
 * A reusable jQueryUI Dialog component (http://jqueryui.com/dialog/).
 * Adapted from http://jsbin.com/vepidi/1/edit?html,js,output
 *
 *
 * Explanation:
 *
 * This component encapsulates some tricky logic needed to allow jQueryUI's
 * DOM manipulation to co-exist with React's render lifecycle. The basic idea
 * is that this component will manually render its child component (see the
 * handlePropsChanged() method defined below). This essentially creates a new
 * render tree which is a sibling of the current tree, and  whose root is the
 * child component passed to this component.
 *
 * The naive approach, which would be to add this to your component:
 *
 *     componentDidMount() {
 *         $(this.getDOMNode()).dialog(opts);
 *     }
 *
 * will cause React to bail since the DOM is changed without its knowledge.
 * This breaks the DOM diffing React requires to maintain its virtual DOM.
 *
 *
 * Example that opens a dialog when a button is clicked:
 *
 *     var MyComponent = React.createClass({
 *
 *         handleShowDialogClick() {
 *           this.setState({
 *             dialogOpen: true
 *           });
 *         },
 *
 *         handleDialogClose() {
 *           this.setState({
 *             dialogOpen: false
 *           });
 *         },
 *
 *         handleFormSubmit(e) {
 *           e.preventDefault();
 *           this.setState({
 *             dialogOpen: false
 *           });
 *           Actions.updateName(this.state.name);
 *         },
 *
 *         handleNameChange(e) {
 *           this.setState({
 *             name: e.getDOMNode().value
 *           });
 *         },
 *
 *         render() {
 *           return (
 *             <div>
 *               <button onClick={this.handleShowDialogClick}>Open dialog</button>
 *
 *               <Dialog open={this.state.dialogOpen} onClose={this.handleDialogClose} title="Enter Your Name">
 *                 <form onSubmit={this.handleFormSubmit}>
 *                   <input onChange={this.handleNameChange} value={this.state.name}/>
 *                   <input type="submit"/>
 *                 </form>
 *               </Dialog>
 *
 *             </div>
 *           );
 *         }
 *     });
 *
 */
class Dialog extends Component<Props> {

  static defaultProps = {
    open: false,
    modal: true,
    title: '',
    width: "auto",
    height: "auto",
    draggable: false,
    resizable: false,
    className: ''
  };

  node: HTMLElement;

  /**
   * Render the child component then open or close dialog
   */
  handlePropsChanged() {
    render(
      <div onKeyDown={(e: React.KeyboardEvent<HTMLDivElement>) => this.handleKeyDown(e)}>
        {this.props.children}
      </div>,
      this.node
    );
    if (this.props.open) {
      $(this.node).dialog('open');
    }
    else {
      $(this.node).dialog('close');
    }
  }

  handleKeyDown(e: React.KeyboardEvent<HTMLElement>) {
    if (e.key === 'Escape') {
      $(this.node).dialog('close');
    }
  }

  componentDidUpdate() {
    this.handlePropsChanged();
  }

  /**
   * At this point, the DOM node has been created, so we can call the jQueryUI
   * dialog plugin, and cache a reference to the instance (this.dialog). Then,
   * we will call handlePropsChanged() to finish off the rendering.
   */
  componentDidMount() {
    this.node = findDOMNode(this);
    var options = {
      modal: this.props.modal,
      close: () => {
        if (this.props.onClose) this.props.onClose();
        document.body.style.overflow = '';
      },
      open: () => {
        if (this.props.onOpen) this.props.onOpen();
        document.body.style.overflow = 'hidden';
      },
      title: this.props.title,
      autoOpen: false,
      width: this.props.width,
      height: this.props.height,
      draggable: this.props.draggable,
      resizable: this.props.resizable,
      dialogClass: 'wdk-Dialog ' + this.props.className,
      position: { my: 'top', at: 'top+100', of: window, collision: 'fit' },
      closeOnEscape: false
    };
    $(this.node).dialog(options as any); // cast options to `any` since we are using an older version of jQueryUI
    this.handlePropsChanged();
  }

  /**
   * Destroy the dialog instance. This will also unmount the child component,
   * which will cause its componentWillUnmount hook to be called.
   */
  componentWillUnmount() {
    $(this.node).dialog('destroy');
    unmountComponentAtNode(this.node);
  }

  /**
   * We only render a single div. Notably, the child component is not rendered
   * here. It is rendered in the handlePropsChanged() method, which is how we
   * can handle DOM manipulation outside of the React lifecycle (which is what
   * we are doing with the jQueryUI plugin).
   */
  render() {
    return <div/>;
  }

}

export default wrappable(Dialog);
