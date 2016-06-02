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
import React from 'react';
import {wrappable} from '../utils/componentUtils';

type Props = {
  title: string;
};

class Doc extends React.Component<Props, void> {

  componentDidMount() {
    document.title = this.props.title;
  }

  componentDidUpdate() {
    document.title = this.props.title;
  }

  render() {
    return (
      <div>{this.props.children}</div>
    );
  }

}

export default wrappable(Doc);