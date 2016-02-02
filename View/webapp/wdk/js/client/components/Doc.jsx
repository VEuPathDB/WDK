/**
 * Use this component to set attributes on `window.document`. Currently, only
 * the title attribute is supported.
 *
 * Example:
 *
 *     <Doc title="My Title">
 *       <Child items={items} ... />
 *     </Doc>
 *
 */
import { Component } from 'react';
import { wrappable } from '../utils/componentUtils';


class Doc extends Component {

  componentDidMount() {
    document.title = this.props.title;
  }

  componentDidUpdate() {
    document.title = this.props.title;
  }

  render() {
    return (
      <div className="wdk-RootContainer">
        {this.props.children}
      </div>
    );
  }

}


export default wrappable(Doc);
