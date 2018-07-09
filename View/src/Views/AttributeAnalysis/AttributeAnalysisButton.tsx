import './AttributeAnalysis.scss';

import React from 'react';

import Loading from 'Components/Loading/Loading';
import Dialog from 'Components/Overlays/Dialog';
import { DispatchAction } from 'Core/CommonTypes';
import { Action } from 'Utils/ActionCreatorUtils';
import { makeClassNameHelper } from 'Utils/ComponentUtils';
import { Reporter } from 'Utils/WdkModel';

import { State } from './AttributeAnalysisStore';
import {
  AttributeReportCancelled,
  AttributeReportRequested,
  ScopedAnalysisAction,
} from './BaseAttributeAnalysis/BaseAttributeAnalysisActions';

const cx = makeClassNameHelper('AttributeAnalysis');

type Props = {
  stepId: number;
  reporter: Reporter;
  dispatch: DispatchAction;
  state: State['analyses'][string];
  ReporterComponent: React.ReactType<{ state: State['analyses'][string]; dispatch: DispatchAction }>;
}

export default class AttributeAnalysisButton extends React.Component<Props> {

  dispatchScopedAction = (action: Action) => {
    const { reporter, stepId } = this.props;
    this.props.dispatch(ScopedAnalysisAction.create({ action, reporter, stepId }));
  }

  loadReport = () => {
    this.dispatchScopedAction(AttributeReportRequested.create({
      reporterName: this.props.reporter.name,
      stepId: this.props.stepId
    }));
  }

  unloadReport = () => {
    this.dispatchScopedAction(AttributeReportCancelled.create());
  }

  render() {
    const { reporter, state, ReporterComponent } = this.props;

    return (
      <React.Fragment>
        <button type="button" onClick={() => this.loadReport()}>
          {reporter.displayName}
        </button>
        {state && state.data.status !== 'idle'
          ? <Dialog modal open onClose={() => this.unloadReport()} className={cx()} title={`${reporter.displayName}`}>
              {state && state.data.status === 'success'
                ? <ReporterComponent state={state} dispatch={this.dispatchScopedAction} />
                : <Loading />
              }
            </Dialog>
          : null
        }
      </React.Fragment>
    );
  }

}
