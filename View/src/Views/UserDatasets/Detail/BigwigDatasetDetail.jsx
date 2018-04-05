import React from 'react';

import Icon from 'Components/Icon/IconAlt';
import { Mesa, MesaState } from 'mesa';
import { makeClassifier } from 'Views/UserDatasets/UserDatasetUtils';
import UserDatasetDetail from 'Views/UserDatasets/Detail/UserDatasetDetail';
import BigwigGBrowseUploader from 'Views/UserDatasets/Detail/BigwigGBrowseUploader';

const classify = makeClassifier('UserDatasetDetail', 'BigwigDatasetDetail');

class BigwigDatasetDetail extends UserDatasetDetail {
  constructor (props) {
    super(props);
    this.renderTracksSection = this.renderTracksSection.bind(this);
    this.getTracksTableColumns = this.getTracksTableColumns.bind(this);
  }

  getTracksTableColumns () {
    const { userDataset, appUrl } = this.props;
    const { id, ownerUserId } = userDataset;
    return [
      {
        key: 'datafileName',
        name: 'Filename',
        renderCell: ({ row }) => <code>{row.datafileName}</code>
      },
      {
        key: 'main',
        name: 'GBrowse Status',
        renderCell: ({ row }) => <BigwigGBrowseUploader {...row} datasetId={id} appUrl={appUrl} />
      }
    ];
  }

  renderTracksSection () {
    const { userDataset, appUrl, projectName } = this.props;

    const { type } = userDataset;

    const rows = Array.isArray(type.data) ? type.data : [];
    const columns = this.getTracksTableColumns({ userDataset, appUrl });
    const tracksTableState = MesaState.create({ rows, columns });

    return !rows.length ? null : userDataset.isInstalled
      ? (
      <section>
        <h1>Use This Dataset in {userDataset.projects.join(', ')}</h1>
        <h3 className={classify('SectionTitle')}>
          <Icon fa="bar-chart"/>
          GBrowse Tracks
        </h3>
        <div className="TracksTable">
          <Mesa state={tracksTableState}/>
        </div>
      </section>
    ) : (
      <section>
        This dataset isn't installed to {projectName} or contains no files.
      </section>
    );
  }

  getPageSections () {
    return [ ...super.getPageSections(), this.renderTracksSection ];
  }
};

export default BigwigDatasetDetail;
