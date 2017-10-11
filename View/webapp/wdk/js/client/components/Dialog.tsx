import React, {Component, Children, ReactChild, ReactNode} from 'react';
import Popup from './Popup';
import Icon from './Icon';
import Resizable from './Resizable';
import { wrappable } from '../utils/componentUtils';

type Action = {
  element: ReactNode
}

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
    return (
      <Popup
        className="wdk-DialogPopupWrapper"
        open={this.props.open}
        dragHandleSelector={() => this.headerNode as Element}
      >
        <Resizable onResize={size => this.handleResize(size)}>
          <div className={'wdk-Dialog' + (this.props.modal ? ' wdk-Dialog__modal' : '')} >
            <div ref={this.setHeaderNodeRef} className="wdk-DialogHeader" >
              <div className="wdk-DialogTitle">{this.props.title}</div>
              {buttons}
            </div>
            <div className="wdk-DialogContent" style={this.state.contentSize}>
              {this.props.children}
            </div>
          </div>
        </Resizable>
      </Popup>
    );
  }

}

export default wrappable(Dialog);
