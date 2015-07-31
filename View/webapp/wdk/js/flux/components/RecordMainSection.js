import React from 'react';
import RecordMainCategorySection from './RecordMainCategorySection';
import wrappable from '../utils/wrappable';

let RecordMainSection = React.createClass({

  mixins: [ React.addons.PureRenderMixin ],

  getDefaultProps() {
    return {
      depth: 1
    };
  },

  render() {
    let {
      depth,
      record,
      recordClass,
      categories,
      collapsedCategories,
      hiddenCategories,
      recordActions
    } = this.props;

    if (categories == null) return null;

    return (
      <div>
        {categories.map(category => {
          if (!hiddenCategories.includes(category.name)) {
            return (
              <RecordMainCategorySection
                depth={depth}
                category={category}
                record={record}
                recordClass={recordClass}
                isCollapsed={collapsedCategories.includes(category.name)}
                recordActions={recordActions}
              >
                <RecordMainSection {...this.props} depth={depth + 1} categories={category.subCategories}/>
              </RecordMainCategorySection>
            );
          }
        })}
      </div>
    );
  }

});

export default wrappable(RecordMainSection);
