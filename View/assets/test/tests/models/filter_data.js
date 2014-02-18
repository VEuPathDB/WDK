describe('wdk.models.filter_data', function() {

  describe('FilterData', function() {

    var filterData;

    beforeEach(function() {
      filterData = new wdk.models.FilterData({
        "filters": [
          { "term": "age", "display": "Age", "type": "number" },
          { "term": "year", "display": "Year", "type": "number" },
          { "term": "host", "display": "Host", "type": "string" }
        ],
        "data": [
          {
            "term": "data1",
            "internal": "d1",
            "display": "Data 1",
            "metadata": {"age": 10, "year": 2012, "host": "human" }
          },
          {
            "term": "data2",
            "internal": "d2",
            "display": "Data 2",
            "metadata": {"age": 7, "year": 2013, "host": "human" }
          },
          {
            "term": "data3",
            "internal": "d3",
            "display": "Data 3",
            "metadata": {"age": 12, "year": 2011, "host": "human" }
          },
          {
            "term": "data4",
            "internal": "d4",
            "display": "Data 4",
            "metadata": {"age": 18, "year": 2012, "host": "human" }
          },
          {
            "term": "data5",
            "internal": "d5",
            "display": "Data 5",
            "metadata": {"age": 4, "year": 2013, "host": "human" }
          },
          {
            "term": "data6",
            "internal": "d6",
            "display": "Data 6",
            "metadata": {"age": 17, "year": 2013, "host": "monkey" }
          },
          {
            "term": "data7",
            "internal": "d7",
            "display": "Data 7",
            "metadata": {"age": 8, "year": 2013, "host": "monkey" }
          },
          {
            "term": "data8",
            "internal": "d8",
            "display": "Data 8",
            "metadata": {"age": 20, "year": 2011, "host": "monkey" }
          },
          {
            "term": "data9",
            "internal": "d9",
            "display": "Data 9",
            "metadata": {"age": 14, "year": 2011, "host": "monkey" }
          },
          {
            "term": "data10",
            "internal": "d10",
            "display": "Data 10",
            "metadata": {"age": 22, "year": 2012, "host": "monkey" }
          },
          {
            "term": "data11",
            "internal": "d11",
            "display": "Data 11",
            "metadata": {"age": 7, "year": 2013, "host": "squirrel" }
          },
          {
            "term": "data12",
            "internal": "d12",
            "display": "Data 12",
            "metadata": {"age": 15, "year": 2012, "host": "squirrel" }
          },
          {
            "term": "data13",
            "internal": "d13",
            "display": "Data 13",
            "metadata": {"age": 8, "year": 2012, "host": "squirrel" }
          },
          {
            "term": "data14",
            "internal": "d14",
            "display": "Data 14",
            "metadata": {"age": 17, "year": 2013, "host": "squirrel" }
          },
          {
            "term": "data15",
            "internal": "d15",
            "display": "Data 15",
            "metadata": {"age": 6, "year": 2012, "host": "squirrel" }
          },
          {
            "term": "data16",
            "internal": "d16",
            "display": "Data 16",
            "metadata": {"age": 4, "year": 2010, "host": "fox" }
          },
          {
            "term": "data17",
            "internal": "d17",
            "display": "Data 17",
            "metadata": {"age": 12, "year": 2013, "host": "fox" }
          },
          {
            "term": "data18",
            "internal": "d18",
            "display": "Data 18",
            "metadata": {"age": 11, "year": 2012, "host": "fox" }
          },
          {
            "term": "data19",
            "internal": "d19",
            "display": "Data 19",
            "metadata": {"age": 9, "year": 2012, "host": "fox" }
          },
          {
            "term": "data20",
            "internal": "d20",
            "display": "Data 20",
            "metadata": {"age": 9, "year": 2014, "host": "fox" }
          },
          {
            "term": "data21",
            "internal": "d21",
            "display": "Data 21",
            "metadata": {"age": 14, "year": 2013, "host": "human" }
          }
        ]
      });
    });

    afterEach(function() {
      filterData = null;
    });

    describe('addFilter', function() {
      it('should apply a single filter', function() {
        function ageOver15(datum) {
          return datum.metadata.age > 15;
        }
        filterData.addFilter(ageOver15);

        filterData.getData().forEach(function(datum) {
          expect(datum.metadata.age).to.be.above(15);
        });
      });

      it('should apply multiple filters', function() {
        function ageOver15(datum) {
          return datum.metadata.age > 15;
        }

        function hostIsMonkey(datum) {
          return datum.metadata.host == 'monkey';
        }

        filterData.addFilter(ageOver15);
        filterData.addFilter(hostIsMonkey);

        filterData.getData().forEach(function(datum) {
          expect(datum.metadata.age).to.be.above(15);
          expect(datum.metadata.host).to.equal('monkey');
        });
      });
    });

    describe('removeFilter', function() {
      it('should remove filters', function() {
        function ageOver15(datum) {
          return datum.metadata.age > 15;
        }
        var key = filterData.addFilter(ageOver15);
        filterData.removeFilter(key);
        expect(filterData.getData()).to.equal(filterData.get('data'));
      });

      it('should cache applied filters', function() {
        function ageOver15(datum) {
          return datum.metadata.age > 15;
        }
        filterData.addFilter(ageOver15);
        filterData.getData();
        var cacheId = filterData._cachedData.cacheId;
        filterData.getData();
        expect(cacheId).to.equal(filterData._cachedData.cacheId);
      });
    });

    describe('values', function() {
      it('should return the values for a specified field', function() {
        console.log(filterData.values('age'));
        console.log(filterData.values('year'));
        console.log(filterData.values('host'));
      });
    });

  });

});
