describe('wdk.models.filter', function() {

  describe('FilterService', function() {

    var filterService;

    beforeEach(function() {
      filterService = new wdk.models.filter.FilterService;
    });

    afterEach(function() {
      filterService = filter = null;
    });

    it('should add filters', function() {
      var filter = filterService.filters.add({ field: 'age', operation: 'between', min: 10, max: 20 });
      expect(filterService.filters.first()).to.equal(filter);
    });

    it('should remove filters', function() {
      var filter = filterService.filters.add({ field: 'age', operation: 'between', min: 10, max: 20 });
      filterService.filters.remove(filter);
      expect(filterService.filters.length).to.equal(0);
    });

    it('should describe filters', function() {
      var filter = filterService.filters.add({ field: 'age', operation: 'between', min: 10, max: 20 });
      expect(filter.toString()).to.equal('Age is between 10 and 20');
    });

  });

  describe('LocalFilterService', function() {
    var filterService;

    beforeEach(function() {
      filterService = new wdk.models.filter.LocalFilterService({
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

    it('should filter by equality', function(done) {
      filterService.on('change:data', function(filterService, data) {
        expect(data).to.have.length.above(0);
        data.forEach(function(d) {
          expect(d.metadata.host).to.equal('human');
        });
        done();
      });

      filterService.filters.add({
        field: 'host',
        operation: 'equals',
        values: ['human']
      });

      filterService.applyFilters();
    });

    it('should filter by membership', function(done) {
      filterService.on('change:data', function(filterService, data) {
        expect(data).to.have.length.above(0);
        var foxes = data.filter(function(d){return d.metadata.host === 'fox'});
        var humans = data.filter(function(d){return d.metadata.host === 'human'});
        expect(foxes).to.have.length.above(1);
        expect(humans).to.have.length.above(1);
        done();
      });

      filterService.filters.add({
        field: 'host',
        operation: 'equals',
        values: ['human', 'fox']
      });

      filterService.applyFilters();
    });

    it('should filter by range', function(done) {
      filterService.on('change:data', function(filterService, data) {
        expect(data).to.have.length.above(0);
        data.forEach(function(d) {
          expect(d.metadata.age).to.be.within(10, 20);
        });
        done();
      });

      filterService.filters.add({
        field: 'age',
        operation: 'between',
        min: 10,
        max: 20
      });

      filterService.applyFilters();
    });

    it('should filter by lower bound', function(done) {
      filterService.on('change:data', function(filterService, data) {
        expect(data).to.have.length.above(0);
        data.forEach(function(d) {
          expect(d.metadata.age).to.be.at.least(10);
        });
        done();
      });

      filterService.filters.add({
        field: 'age',
        operation: 'between',
        min: 10
      });

      filterService.applyFilters();
    });

    it('should filter by upper bound', function(done) {
      filterService.on('change:data', function(filterService, data) {
        expect(data).to.have.length.above(0);
        data.forEach(function(d) {
          expect(d.metadata.age).to.be.at.most(20);
        });
        done();
      });

      filterService.filters.add({
        field: 'age',
        operation: 'between',
        max: 20
      });

      filterService.applyFilters();
    });

  });

});
