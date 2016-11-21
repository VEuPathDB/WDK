import React from 'react';
import { wrappable } from '../utils/componentUtils';
import { UserDataset } from '../utils/WdkModel';
import { bytesToHuman } from '../utils/Converters';

type Props = {
  userDataset: UserDataset
};

const makeClassName = (element?: string, modifier?: string) =>
  'wdk-UserDataset' +
  (element ? `${element}` : ``) +
  (modifier ? `__${modifier}` : ``);

const UserDatasetItem = ({userDataset}: Props) =>
  <div className={makeClassName()}>
    <h1 className={makeClassName('Heading')}>{userDataset.meta.name}</h1>
    <p className={makeClassName('Summary')}>{userDataset.meta.summary}</p>

    <h2 className={makeClassName('SectionHeading')}>Description</h2>
    <div>{userDataset.type.name} {userDataset.meta.description}</div>

    <h2 className={makeClassName('SectionHeading')}>Version</h2>
    <div>{userDataset.type.name} {userDataset.type.version}</div>

    <h2 className={makeClassName('SectionHeading')}>Data files</h2>
    <ul>
      {userDataset.datafiles.map(datafile =>
        <li>{datafile.name} ({bytesToHuman(datafile.size)})</li>
      )}
    </ul>

    <h2 className={makeClassName('SectionHeading')}>Projects</h2>
    <ul>
      {userDataset.projects.map(project =>
        <li>{project}</li>
      )}
    </ul>

    <h2 className={makeClassName('SectionHeading')}>Dependencies</h2>
    <ul>
      {userDataset.dependencies.map(dependency =>
        <li>{dependency.resourceDisplayName} (v {dependency.resourceVersion})</li>
      )}
    </ul>

    <h2 className={makeClassName('SectionHeading')}>Size</h2>
    <div>{bytesToHuman(userDataset.size)}</div>

    <h2 className={makeClassName('SectionHeading')}>Created</h2>
    <div>{new Date(userDataset.created).toDateString()}</div>

    <h2 className={makeClassName('SectionHeading')}>Modified</h2>
    <div>{new Date(userDataset.modified).toDateString()}</div>

    {/* TODO For development - remove before release */}
    <div style={{ marginTop: '1em' }}>
      <details>
        <summary>Raw Data</summary>
        <pre>{JSON.stringify(userDataset, null, 4)}</pre>
      </details>
    </div>
  </div>

export default wrappable(UserDatasetItem);
