import React, { Component, Children, ReactChild, ReactNode } from 'react';
import Popup from 'Components/Overlays/Popup';
import Icon from 'Components/Icon/Icon';
import Resizable from 'Components/Display/Resizable';
import { wrappable, makeClassNameHelper } from 'Utils/ComponentUtils';

let c = makeClassNameHelper('wdk-Dialog');
let c2 = makeClassNameHelper(' ');

type Props = {
  open: boolean;
  children: ReactChild;
  modal?: boolean;
  title?: ReactNode;
  buttons?: ReactNode[];
  draggable?: boolean;
  resizable?: boolean;
  className?: string;
  onOpen?: () => void;
  onClose?: () => void;
};

class Dialog extends Component<Props> {

  headerNode: Element | null;

  constructor(props: Props) {
    super(props);
    this.setHeaderNodeRef = this.setHeaderNodeRef.bind(this);
  }

  makeClassName(suffix = '', ...modifiers: any[]) {
    let { className } = this.props;
    return c(suffix, ...modifiers) + (
      className ? c2(className + suffix, ...modifiers) : ''
    );
  }

  setHeaderNodeRef(node: Element | null) {
    this.headerNode = node;
  }

  blockScrollingIfModalOpen() {
    let classes = document.body.classList;
    if (this.props.modal && this.props.open) classes.add('wdk-ModalOpen');
    else classes.remove('wdk-ModalOpen');
  }

  componentDidMount() {
    this.blockScrollingIfModalOpen();
  }

  componentDidUpdate() {
    this.blockScrollingIfModalOpen();
  }

  componentWillUnmount() {
    document.body.classList.remove('wdk-ModalOpen');
  }

  render () {
    let {
      onClose = () => {},
      buttons = [(
        <button key="close" type="button" onClick={() => onClose()}>
          <Icon type="close"/>
        </button>
      )]
    } = this.props;

    let content = (
      <div className={this.makeClassName('', this.props.modal && 'modal')} >
        <div ref={this.setHeaderNodeRef} className={this.makeClassName('Header')} >
          <div className={this.makeClassName('Title')}>{this.props.title}</div>
          {buttons}
        </div>
        <div className={this.makeClassName('Content')}>
          {this.props.children}
        </div>
      </div>
    );

    if (this.props.resizable) {
      content = (
        <Resizable>
          {content}
        </Resizable>
      );
    }

    return (
      <Popup
        className={this.makeClassName('PopupWrapper')}
        open={this.props.open}
        dragHandleSelector={() => this.headerNode as Element}
      >
        {content}
      </Popup>
    );
  }

}

export default wrappable(Dialog);
