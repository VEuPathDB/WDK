import { DispatchAction } from '../../Core/CommonTypes';
import { memoize } from 'lodash';
import React from 'react';

import AbstractViewController from 'Core/Controllers/AbstractViewController';
import { Action } from 'Utils/ActionCreatorUtils';
import { wrappable } from 'Utils/ComponentUtils';
import { Seq } from 'Utils/IterableUtils';
import { RecordClass, Reporter } from 'Utils/WdkModel';

import AttributeAnalysisButton from './AttributeAnalysisButton';
import { AttributeAnalysisStore, State } from './AttributeAnalysisStore';
import { AttributeReportCancelled, AttributeReportRequested, ScopedAnalysisAction } from './BaseAttributeAnalysis/BaseAttributeAnalysisActions';
import HistogramAnalysis from './HistogramAnalysis/HistogramAnalysis';
import WordCloudAnalysis from './WordCloudAnalysis/WordCloudAnalysis';

type ViewState = {
  reporter?: Reporter;
  analysis?: State['analyses'][string]
}

class AttributeAnalysisButtonController extends AbstractViewController<
  ViewState,
  AttributeAnalysisStore,
  {},
  {
    recordClassName: string;
    reporterName: string;
    stepId: number
  }
> {

  getStoreClass() {
    return AttributeAnalysisStore;
  }

  getStateFromStore() {
    const { globalData, analyses } = this.store.getState();
    const reporter = Seq.from(globalData.recordClasses)
      .filter(recordClass => recordClass.name === this.props.recordClassName)
      .flatMap(recordClass => recordClass.formats)
      .find(format => format.name === this.props.reporterName);
    const key = `${this.props.stepId}__${this.props.reporterName}`;
    const analysis = analyses && analyses[key];
    return { reporter, analysis
    }
  }

  getReporterComponent(reporter: Reporter): React.ReactType | undefined {
    // TODO Replace with configuration lookup
    switch(reporter.type) {
      case 'wordCloud': return WordCloudAnalysis;
      case 'histogram': return HistogramAnalysis;
      default: return undefined;
    }
  }

  renderView() {
    const { reporter, analysis } = this.state;

    if (reporter == null) return null;

    const ReporterComponent = this.getReporterComponent(reporter);

    if (ReporterComponent == null) return null;

    return (
      <AttributeAnalysisButton
        stepId={this.props.stepId}
        reporter={reporter}
        state={analysis}
        dispatch={this.dispatchAction}
        ReporterComponent={ReporterComponent}
      />
    );
  }

}

export default wrappable(AttributeAnalysisButtonController);
