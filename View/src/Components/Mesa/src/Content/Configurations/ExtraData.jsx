import Datasets from 'Content/Data/Datasets';

const ExtraData = {
  label: 'Extra Data + Inline Mode!',
  rows: [
    ...Datasets, ...Datasets,
    ...Datasets, ...Datasets,
    ...Datasets, ...Datasets,
    ...Datasets, ...Datasets,
    ...Datasets, ...Datasets
  ],
  options: {
    title: '10x Regular Data!',
    inline: true,
    paginate: true,
    inlineMaxWidth: '300px',
    columnDefaults: {
      truncated: false,
      overflowHeight: '2rem'
    }
  }
};

export default ExtraData;
