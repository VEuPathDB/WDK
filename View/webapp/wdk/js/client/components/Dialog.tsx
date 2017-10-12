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

type State = {
  contentSize: {
    height?: number,
    width?: number
  }
}

class Dialog extends Component<Props, State> {

  headerNode: Element | null;

  constructor(props: Props) {
    super(props);
    this.setHeaderNodeRef = this.setHeaderNodeRef.bind(this);
    this.state = { contentSize: {} };
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

  handleResize(size: { height: number, width: number }) {
    this.setState({
      contentSize: {
        height: this.headerNode ? size.height - this.headerNode.clientHeight : undefined,
        width: size.width
      }
    });
  }

  render () {
    let {
      onClose = () => {},
      buttons = [(
        <button type="button" onClick={() => onClose()}>
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
        <div className={this.makeClassName('Content')} style={this.state.contentSize}>
          {this.props.children}
        </div>
      </div>
    );

    if (this.props.resizable) {
      content = (
        <Resizable onResize={size => this.handleResize(size)}>
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
