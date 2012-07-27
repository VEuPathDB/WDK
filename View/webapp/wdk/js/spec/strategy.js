describe("wdk.strategy namespace", function() {
"use strict";

  var ns = wdk.strategy,
      Strategy,
      Step,
      loadModel,
      strats,
      testData,
      strategyJson,
      strategy,
      subStrategy,
      steps,
      firstStep,
      lastStep;

  beforeEach(function() {
    // mock up some data
    Strategy = ns.Strategy,
    Step = ns.Step,
    loadModel = ns.loadModel,
    strats = ns.strats,
    testData = wdk.test.strategy.testData;

    // create test strat
    // based on http://plasmodb.org/plasmo/im.do?s=57c0cf7dabba1408
    strategyJson = testData.strategies["59401bb222922a61a13f1a224391ab39"];

    loadModel(strategyJson, 1, testData.state);
    strategy = strats["1"];
    subStrategy = strats["1.1"];

    // assuming steps get added in-order in Strategy.initSteps
    firstStep = strategy.Steps[0];
    lastStep = strategy.Steps[strategy.Steps.length - 1];
    //child_step = strategy.Steps[1].step
    
  });

  describe("A Strategy", function() {

    it("should be able to get a Step by front id", function() {
      expect(strategy.getStep(firstStep.frontId, true)).toEqual(firstStep);
    });

    it("should be able to get a Step by back id", function() {
      expect(strategy.getStep(firstStep.back_step_Id, false)).toEqual(firstStep);
    });

    it("should be able to find a parent Step by front id", function() {
      var foundObject = subStrategy.findParentStep(firstStep.frontId, true);
      expect(foundObject.stp).toEqual(firstStep);
      expect(foundObject.str).toEqual(strategy);
    });

    it("should be able to find a parent Step by back id", function() {
      var foundObject = subStrategy.findParentStep(firstStep.back_step_Id, false);
      expect(foundObject.stp).toEqual(firstStep);
      expect(foundObject.str).toEqual(strategy);
    });

    it("should be able to find a Step by front id", function() {
      var foundObject = strategy.findStep(firstStep.frontId, true);
      expect(foundObject.str).toEqual(strategy);
      expect(foundObject.stp).toEqual(firstStep);
    });

    it("should be able to find a Step by back id", function() {
      var foundObject = strategy.findStep(firstStep.back_step_Id, false);
      expect(foundObject.str).toEqual(strategy);
      expect(foundObject.stp).toEqual(firstStep);
    });

    it("should be able to get the last Step", function() {
      expect(strategy.getLastStep()).toEqual(lastStep);
    });

    it("should be able to find its depth", function() {
      // TODO - What is depth() calculating?
      // Maybe depth of step?
      expect(strategy.depth()).toBe(0);
      expect(subStrategy.depth()).toBe(1);
    });

    xit("should be able to initialize Steps", function() {
      // TODO - this should be a private member function?
    });

  });

  // left the following suites, disabled, for future reference
  xdescribe("when song has been paused", function() {
    beforeEach(function() {
      player.play(song);
      player.pause();
    });

    it("should indicate that the song is currently paused", function() {
      expect(player.isPlaying).toBeFalsy();

      // demonstrates use of 'not' with a custom matcher
      expect(player).not.toBePlaying(song);
    });

    it("should be possible to resume", function() {
      player.resume();
      expect(player.isPlaying).toBeTruthy();
      expect(player.currentlyPlayingSong).toEqual(song);
    });
  });

  // demonstrates use of spies to intercept and test method calls
  xit("tells the current song if the user has made it a favorite", function() {
    spyOn(song, 'persistFavoriteStatus');

    player.play(song);
    player.makeFavorite();

    expect(song.persistFavoriteStatus).toHaveBeenCalledWith(true);
  });

  //demonstrates use of expected exceptions
  xdescribe("#resume", function() {
    it("should throw an exception if song is already playing", function() {
      player.play(song);

      expect(function() {
        player.resume();
      }).toThrow("song is already playing");
    });
  });

});
