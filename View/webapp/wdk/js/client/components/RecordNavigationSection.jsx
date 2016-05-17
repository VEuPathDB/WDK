import React from 'react';
import classnames from 'classnames';
import {includes, memoize} from 'lodash';
import RecordNavigationSectionCategories from './RecordNavigationSectionCategories';
import { postorderSeq } from '../utils/TreeUtils';
import { wrappable, PureComponent } from '../utils/componentUtils';
import { getPropertyValues, nodeHasProperty } from '../utils/OntologyUtils';
import { getId, getDisplayName } from '../utils/CategoryUtils';
import { parseSearchQueryString, areTermsInString } from '../utils/SearchUtils';

class RecordNavigationSection extends PureComponent {

  render() {
    let { collapsedSections, heading, navigationQuery, navigationSubcategoriesExpanded } = this.props;
    let searchQueryTerms = parseSearchQueryString(navigationQuery);
    let categoryWordsMap = makeCategoryWordsMap(this.props.categoryTree);
    let expandClassName = classnames({
      'wdk-RecordNavigationExpand fa': true,
      'fa-plus-square': !navigationSubcategoriesExpanded,
      'fa-minus-square': navigationSubcategoriesExpanded
    });

    return (
      <div className="wdk-RecordNavigationSection">
        <h2 className="wdk-RecordNavigationSectionHeader">
          <button type="button" className={expandClassName}
            onClick={() => {
              this.props.onNavigationSubcategoryVisibilityChange(
                !this.props.navigationSubcategoriesExpanded);
            }}
          /> {heading}
        </h2>
        <div className="wdk-RecordNavigationSearch">
          <input
            className="wdk-RecordNavigationSearchInput"
            placeholder={'Search ' + heading}
            type="text"
            value={navigationQuery}
            onChange={e => {
              this.props.onNavigationQueryChange(e.target.value);
              this.props.onNavigationSubcategoryVisibilityChange(true);
            }}
          />
        </div>
        <div className="wdk-RecordNavigationCategories">
          <RecordNavigationSectionCategories
            record={this.props.record}
            recordClass={this.props.recordClass}
            categories={this.props.categoryTree.children}
            onSectionToggle={this.props.onSectionToggle}
            showChildren={navigationSubcategoriesExpanded}
            isCollapsed={category => includes(collapsedSections, getId(category))}
            isVisible={category => areTermsInString(searchQueryTerms, categoryWordsMap.get(category.properties))}
          />
        </div>
      </div>
    );
  }
}

RecordNavigationSection.propTypes = {
  collapsedSections: React.PropTypes.array,
  onSectionToggle: React.PropTypes.func,
  heading: React.PropTypes.node
};

RecordNavigationSection.defaultProps = {
  onSectionToggle: function noop() {},
  heading: 'Contents'
};

export default wrappable(RecordNavigationSection);

let makeCategoryWordsMap = memoize((root) =>
  postorderSeq(root).reduce((map, node) => {
    let words = [];

    // add current node's displayName and description
    words.push(
      getDisplayName(node),
      ...getPropertyValues('hasDefinition', node),
      ...getPropertyValues('hasExactSynonym', node),
      ...getPropertyValues('hasNarrowSynonym', node)
    );

    // add displayName and desription of attribute or table
    if (nodeHasProperty('targetType', 'attribute', node) || nodeHasProperty('targetType', 'table', node)) {
      words.push(node.wdkReference.displayName, node.wdkReference.description);
    }

    // add words from any children
    for (let child of node.children) {
      words.push(map.get(child.properties));
    }

    return map.set(node.properties, words.join('\0').toLowerCase());
  }, new Map));
