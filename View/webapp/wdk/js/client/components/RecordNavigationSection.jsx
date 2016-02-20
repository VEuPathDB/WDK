import React from 'react';
import PureRenderMixin from 'react-addons-pure-render-mixin';
import classnames from 'classnames';
import includes from 'lodash/collection/includes';
import memoize from 'lodash/function/memoize';
import RecordNavigationSectionCategories from './RecordNavigationSectionCategories';
import { postorderSeq } from '../utils/TreeUtils';
import { wrappable } from '../utils/componentUtils';
import {
  getDisplayName,
  getPropertyValue,
  getPropertyValues,
  nodeHasProperty
} from '../utils/OntologyUtils';

let RecordNavigationSection = React.createClass({

  propTypes: {
    collapsedCategories: React.PropTypes.array,
    onCategoryToggle: React.PropTypes.func,
    heading: React.PropTypes.node
  },

  mixins: [ PureRenderMixin ],

  getInitialState() {
    return {
      navigationExpanded: false,
      navigationQuery: ''
    };
  },

  getDefaultProps() {
    return {
      onCategoryToggle: function noop() {},
      heading: 'Contents'
    };
  },

  render() {
    let { navigationExpanded, navigationQuery } = this.state;
    let { collapsedCategories, heading } = this.props;
    let navigationQueryLower = navigationQuery.toLowerCase();
    let categoryWordsMap = makeCategoryWordsMap(this.props.recordClass, this.props.categoryTree);
    let expandClassName = classnames({
      'wdk-RecordNavigationExpand fa': true,
      'fa-plus-square': !navigationExpanded,
      'fa-minus-square': navigationExpanded
    });

    return (
      <div className="wdk-RecordNavigationSection">
        <h2 className="wdk-RecordNavigationSectionHeader">
          <button className={expandClassName}
            onClick={() => void this.setState({ navigationExpanded: !navigationExpanded })}
          /> {heading}
        </h2>
        <div className="wdk-RecordNavigationSearch">
          <input
            className="wdk-RecordNavigationSearchInput"
            placeholder={'Search ' + heading}
            type="text"
            value={navigationQuery}
            onChange={e => {
              this.setState({
                navigationQuery: e.target.value,
                navigationExpanded: true
              });
            }}
          />
        </div>
        <div className="wdk-RecordNavigationCategories">
          <RecordNavigationSectionCategories
            record={this.props.record}
            recordClass={this.props.recordClass}
            categories={this.props.categoryTree.children}
            onCategoryToggle={this.props.onCategoryToggle}
            showChildren={navigationExpanded}
            isCollapsed={category => includes(collapsedCategories, getPropertyValue('label', category))}
            isVisible={category => includes(categoryWordsMap.get(category.properties), navigationQueryLower)}
          />
        </div>
      </div>
    );
  }
});

export default wrappable(RecordNavigationSection);

let makeCategoryWordsMap = memoize((recordClass, root) =>
  postorderSeq(root).reduce((map, node) => {
    let words = [];

    // add current node's displayName and description
    words.push(
      getDisplayName(node),
      ...getPropertyValues('hasDefinition', node),
      ...getPropertyValues('hasExactSynonym', node),
      ...getPropertyValues('hasNarrowSynonym', node)
    );

    // add displayName and desription of attribute
    if (nodeHasProperty('targetType', 'attribute', node)) {
      let attribute = recordClass.attributes.find(a => a.name === getPropertyValues('name', node)[0]);
      words.push(attribute.displayName, attribute.description);
    }

    // add displayName and desription of table
    if (nodeHasProperty('targetType', 'table', node)) {
      let table = recordClass.tables.find(a => a.name === getPropertyValues('name', node)[0]);
      words.push(table.displayName, table.description);
    }

    // add words from any children
    for (let child of node.children) {
      words.push(map.get(child.properties));
    }

    return map.set(node.properties, words.join('\0').toLowerCase());
  }, new Map))
