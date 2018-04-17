import React from 'react';

import Events from 'Mesa/Utils/Events';

class Modal extends React.Component {
  constructor (props) {
    super(props);
    this.handleClose = this.handleClose.bind(this);
  }

  componentWillReceiveProps ({ open }) {
    if (!!open === !!this.props.open) return;
    if (!!open && !this.props.open) return this.closeListener = Events.onKey('esc', this.handleClose);
    else Events.remove(this.closeListener);
  }

  handleClose () {
    const { onClose } = this.props;
    return onClose && onClose();
  }

  diffuseClick (event) {
    return event.stopPropagation();
  }

  render () {
    const { open, children, className } = this.props;
    const _className = `Modal ${open ? 'Modal-Open' : 'Modal-Closed'} ${className || ''}`

    return (
      <div className="Modal-Wrapper">
        <div className={_className} onClick={this.handleClose}>
          <div className="Modal-Body" onClick={this.diffuseClick}>
            {children}
          </div>
        </div>
      </div>
    );
  }
};

export default Modal;
