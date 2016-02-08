import React from 'react';
import PureRenderMixin from 'react-addons-pure-render-mixin';
import classnames from 'classnames';
import includes from 'lodash/collection/includes';
import memoize from 'lodash/function/memoize';
import RecordNavigationSectionCategories from './RecordNavigationSectionCategories';
import * as i from '../utils/IterableUtils';
import { postorder as postorderCategories } from '../utils/CategoryTreeIterators';
import { wrappable } from '../utils/componentUtils';

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
      heading: 'Categories'
    };
  },

  render() {
    let { navigationExpanded, navigationQuery } = this.state;
    let { collapsedCategories, heading } = this.props;
    let navigationQueryLower = navigationQuery.toLowerCase();
    let categoryWordsMap = makeCategoryWordsMap(this.props.recordClass);
    let expandClassName = classnames({
      'wdk-RecordNavigationExpand fa': true,
      'fa-plus-square': !navigationExpanded,
      'fa-minus-square': navigationExpanded
    });

    return (
      <div className="wdk-RecordNavigationSection">
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
        <h2 className="wdk-RecordNavigationSectionHeader">
          <button className={expandClassName}
            onClick={() => void this.setState({ navigationExpanded: !navigationExpanded })}
          /> {heading}
        </h2>
        <div className="wdk-RecordNavigationCategories">
          <RecordNavigationSectionCategories
            record={this.props.record}
            recordClass={this.props.recordClass}
            categories={this.props.recordClass.categories}
            onCategoryToggle={this.props.onCategoryToggle}
            showChildren={navigationExpanded}
            isCollapsed={category => includes(collapsedCategories, category.name)}
            isVisible={category => includes(categoryWordsMap.get(category), navigationQueryLower)}
          />
        </div>
      </div>
    );
  }
});

export default wrappable(RecordNavigationSection);

let makeCategoryWordsMap = memoize((recordClass) => {
  return i.reduce((map, category) => {
    let words = [];

    for (let attribute of recordClass.attributes) {
      if (attribute.category == category.name) {
        words.push(attribute.displayName, attribute.description);
      }
    }

    for (let table of recordClass.tables) {
      if (table.category == category.name) {
        words.push(table.displayName, table.description);
      }
    }

    if (category.categories != null) {
      for (let cat of map.keys()) {
        if (category.categories.indexOf(cat) > -1) {
          words.push(map.get(cat));
        }
      }
    }

    words.push(category.displayName, category.description);

    return map.set(category, words.join('\0').toLowerCase());
  }, new Map(), postorderCategories(recordClass.categories));
});
