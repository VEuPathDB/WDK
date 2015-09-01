import React from 'react';
import wrappable from '../utils/wrappable';

let $ = window.jQuery;
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
var Dialog = React.createClass({

  getDefaultProps() {
    return {
      open: false,
      modal: true,
      title: '',
      onOpen() {},
      onClose() {}
    };
  },

  /**
   * Render the child component then open or close dialog
   */
  handlePropsChanged() {
    React.render(
      React.Children.only(this.props.children),
      this.node
    );
    if (this.props.open) {
      $(this.node).dialog('open');
    }
    else {
      $(this.node).dialog('close');
    }
  },

  componentDidUpdate() {
    this.handlePropsChanged();
  },

  /**
   * At this point, the DOM node has been created, so we can call the jQueryUI
   * dialog plugin, and cache a reference to the instance (this.dialog). Then,
   * we will call handlePropsChanged() to finish off the rendering.
   */
  componentDidMount() {
    var options = {
      modal: this.props.modal,
      close: this.props.onClose,
      open: this.props.onOpen,
      title: this.props.title,
      autoOpen: false
    };
    this.node = React.findDOMNode(this);
    $(this.node).dialog(options);
    this.handlePropsChanged();
  },

  /**
   * Destroy the dialog instance. This will also unmount the child component,
   * which will cause its componentWillUnmount hook to be called.
   */
  componentWillUnmount() {
    $(this.node).dialog('destroy');
    React.unmountComponentAtNode(React.findDOMNode(this));
  },

  /**
   * We only render a single div. Notably, the child component is not rendered
   * here. It is rendered in the handlePropsChanged() method, which is how we
   * can handle DOM manipulation outside of the React lifecycle (which is what
   * we are doing with the jQueryUI plugin).
   */
  render() {
    return <div/>;
  }
});

export default wrappable(Dialog);
