import React from 'react';

var $ = window.jQuery;

/**
 * Adapted from http://jsbin.com/vepidi/1/edit?html,js,output
 *
 * Use it like this:
 *
 *     <Dialog open={this.state.dialogIsOpen}
 *       onClose={this.handleDialogClose}
 *       title="Dialog"/>
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

  handlePropsChanged() {
    React.render(
      React.Children.only(this.props.children),
      this.node
    );
    if (this.props.open)
      this.dialog.open();
    else
      this.dialog.close();
  },

  componentDidUpdate() {
    this.handlePropsChanged();
  },

  componentDidMount() {
    this.node = this.getDOMNode();
    var options = {
      modal: this.props.modal,
      close: this.props.onClose,
      open: this.props.onOpen,
      title: this.props.title,
      autoOpen: false
    };
    this.dialog = $(this.node).dialog(options).dialog('instance');
    this.handlePropsChanged();
  },

  componentWillUnmount() {
    this.dialog.destroy();
  },

  render() {
    return <div/>;
  }
});

export default Dialog;
