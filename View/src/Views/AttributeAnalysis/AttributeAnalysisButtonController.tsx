import React from 'react';

import AbstractViewController from 'Core/Controllers/AbstractViewController';
import { GlobalData } from 'Core/State/Stores/GlobalDataStore';
import { Action } from 'Utils/ActionCreatorUtils';
import { wrappable } from 'Utils/ComponentUtils';
import { Seq } from 'Utils/IterableUtils';
import { Reporter } from 'Utils/WdkModel';

import AttributeAnalysisButton from './AttributeAnalysisButton';
import { AttributeAnalysisStore, State } from './AttributeAnalysisStore';
import { ScopedAnalysisAction } from './BaseAttributeAnalysis/BaseAttributeAnalysisActions';

type ViewProps = {
  attributeName: string;
  recordClassName: string;
  reporterName: string;
  stepId: number;
}

class AttributeAnalysisButtonController extends AbstractViewController<
  State,
  AttributeAnalysisStore,
  {},
  ViewProps
> {

  getStoreClass() {
    return AttributeAnalysisStore;
  }

  getStateFromStore() {
    return this.store.getState();
  }

  plugin = this.props.locatePlugin('attributeAnalysis');

  renderView() {
    const { recordClassName, reporterName, stepId } = this.props;
    const { globalData, analyses } = this.state;
    const reporter = Seq.from(globalData.recordClasses)
      .filter(recordClass => recordClass.name === this.props.recordClassName)
      .flatMap(recordClass => recordClass.formats)
      .find(format => format.name === this.props.reporterName);
    const key = `${this.props.stepId}__${this.props.reporterName}`;
    const analysis = analyses && analyses[key];

    if (reporter == null) return null;

    const context = {
      type: 'attributeAnalysis',
      name: reporter.type,
      recordClassName: this.props.recordClassName
    }

    const dispatch = (action: Action) => {
      const { stepId } = this.props;
      this.dispatchAction(ScopedAnalysisAction.create({ action, context, reporter, stepId }));
    }

    return (
      <AttributeAnalysisButton
        recordClassName={this.props.recordClassName}
        stepId={this.props.stepId}
        reporter={reporter}
        analysis={analysis}
        dispatch={dispatch}>
        {this.plugin.render(context, analysis, dispatch)}
      </AttributeAnalysisButton>
    );
  }

}

export default wrappable(AttributeAnalysisButtonController);
