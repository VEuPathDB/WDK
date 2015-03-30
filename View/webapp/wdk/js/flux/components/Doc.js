/**
 * Use this component to set attributes on `window.document`. Currently, only
 * the title attribute is supported.
 *
 * XXX Should this allow arbitrary properties? Title may be the only meaningful
 * property to set.
 *
 * Example:
 *
 *     <Doc title="My Title">
 *       <Child items={items} ... />
 *     </Doc>
 *
 */
import React from 'react';

const Doc = React.createClass({
  propTypes: {
    title: React.PropTypes.string
  },
  componentDidMount() {
    this.setTitle();
  },
  componentDidUpdate() {
    this.setTitle();
  },
  setTitle() {
    const { title } = this.props;
    document.title = title;
  },
  render() {
    return this.props.children;
  }
});

export default Doc;
