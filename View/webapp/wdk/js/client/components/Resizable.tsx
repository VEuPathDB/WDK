import * as React from 'react';
import * as ReactDOM from 'react-dom';
import $ from 'jquery';

type Props = {
  children: React.ReactElement<any>;
  onResize?: (size: { width: number, height: number }) => void;
}

export default class Resizable extends React.Component<Props> {

  componentDidMount() {
    $(ReactDOM.findDOMNode(this.refs.child)).resizable({
      handles: 'all',
      resize: (event, ui) => {
        this.props.onResize && this.props.onResize({
          height: ui.element[0].clientHeight,
          width: ui.element[0].clientWidth
        })
      },
      minWidth: 100,
      minHeight: 100
    });
  }

  componentWillUnmount() {
    $(ReactDOM.findDOMNode(this.refs.child)).resizable('destroy');
  }

  render() {
    return React.cloneElement(this.props.children, {
      ref: 'child'
    })
  }

}
