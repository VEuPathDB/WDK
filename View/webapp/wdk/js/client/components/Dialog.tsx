import React, {Component, Children, ReactChild, ReactNode} from 'react';
import Popup from './Popup';
import Icon from './Icon';
import Resizable from './Resizable';
import { wrappable, makeClassNameHelper } from '../utils/componentUtils';

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
