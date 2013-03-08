/**
 *  Provides the Strategy and Step object constructors,
 *  as well as helper functions for finding object instances.
 *
 *  @namespace model
**/

wdk.util.namespace("window.wdk.strategy.model", function (ns, $) {
  "use strict";

  var Strategy,
      Step,
      getStep,
      getStrategy,
      getSubStrategies,
      getStrategyFromBackId,
      getStepFromBackId,
      findStrategy,
      findStep,
      isLoaded,
      getStrategyJSON,
      getStrategyOBJ;


  /**
   *  A tree of Step objects.
   *  Steps are connected with operators (which are also Steps).
   *
   *  @class Strategy
   *  @cosntructor
  **/
  Strategy = function (frontId, backId, isDisplay) {
    this.frontId = frontId;
    this.backId = backId;
    this.isDisplay = isDisplay;
    this.subStratOrder = {};
  };

  Strategy.prototype.checksum = null;
  Strategy.prototype.JSON = null;
  Strategy.prototype.DIV = null;
  Strategy.prototype.subStratOf = null;
  Strategy.prototype.Steps = [];
  Strategy.prototype.isSaved = false;
  Strategy.prototype.name = null;
  Strategy.prototype.savedName = null;
  Strategy.prototype.importId = null;
  Strategy.prototype.dataType = null;
  Strategy.prototype.displayType = null;
  Strategy.prototype.color = null;
  Strategy.prototype.nonTransformLength = null;

  Strategy.prototype.getStep = function (stepId,isfront) {
    var s;
    for (s in this.Steps) {
      if (isfront) {
        if (this.Steps[s].frontId == stepId)
          return this.Steps[s];
      } else {
        if (this.Steps[s].back_step_Id == stepId ||
            this.Steps[s].back_boolean_Id == stepId) {
          return this.Steps[s];
        }
      }
    }
    return null;
  };

  Strategy.prototype.findParentStep = function (stpId, isFront) {
    if (this.subStratOf === null) {
      return null;
    } else {
      return getStrategy(this.subStratOf).findStep(stpId, isFront);
    }
  };

  Strategy.prototype.findStep = function (stpId, isFront) {
    var s = null,
        st = this.getStep(stpId,isFront);
    if (st) {
      s = {str:this, stp:st};
    } else {
      for (var t in this.subStratOrder) {
        s = getStrategy(this.subStratOrder[t]).findStep(stpId, isFront);
        if (s) break;
      }
    }
    return s;
  };

  Strategy.prototype.getLastStep = function () {
    var s,
        cId = 0;
    for (s in this.Steps) {
      cId = Math.max(this.Steps[s].frontId, cId);
    }
    return this.getStep(cId, true);
  };

  Strategy.prototype.depth = function (stepid, d) {
    var parStrat,
        ssParts,
        parStep,
        subS,
        str,
        r;
    if (this.subStratOf === null) {
      return 0;
    }
    parStrat = this;
    if (stepid === null) {
      d = 1;
      ssParts = this.backId.split("_");
      stepid = ssParts[1];
      parStrat = getStrategyFromBackId(ssParts[0]);
    }
    parStep = parStrat.getStep(stepid, false);
    if (parStep) {
      return 1;
    } else {
      subS = getSubStrategies(parStrat.frontId);
      if (subS.length === 0) {
        return null;
      }
      for (str in subS) {
        r = subS[str].depth(stepid, d);
        if (r) {
          return d + r;
        }
      }
    }
  };

  Strategy.prototype.initSteps = function (steps, ord) {
    var i,
        subId,
        pstp,
        nstp,
        arr = [],
        st = null,
        ssind = 1,
        stepCount = steps.length;

    for (i in steps) {
      if (i != "length" && i != "nonTransformLength") {
        if (steps[i].step) {
          st = new Step(i, steps[i].step.id, steps[i].id, null,
              steps[i].step.answerId);
          st.operation = steps[i].operation;
          if (steps[i].isboolean) {
            st.isboolean = true;
            st.isSpan = false;
          } else {
            st.isSpan = true;
            st.isboolean = false;
            st.operation = "SPAN";
          }
          if (steps[i].step.isCollapsed && steps[i].step.strategy.order > 0) {
            this.subStratOrder[steps[i].step.strategy.order] = wdk.strategy.controller.sidIndex;
            subId = wdk.strategy.controller.loadModel(steps[i].step.strategy, ord + "." + ssind);
            ssind++;
            //this.subStratOrder[steps[i].step.strategy.order] = subId;
            st.child_Strat_Id = subId;
          }
        } else {
          st = new Step(i, steps[i].id, "", null, steps[i].answerId);
          if (steps[i].istransform) {
            st.isTransform = true;
          }
        }
        if (i == stepCount) {
          st.isLast = true;
        }
        if (st.frontId != 1) {
          pstp = steps[parseInt(i, 10)-1];
          if (parseInt(i, 10) - 1 == 1) {
            st.prevStepType = "";
          } else {
            st.prevStepType = (pstp.istransform) ? "transform" : "boolean";
          }
        }
        if (!st.isLast) {
          nstp = steps[parseInt(i, 10) + 1];
          st.nextStepType = (nstp.istransform) ? "transform" : "boolean";
        }
        arr.push(st);
      }
    }
    this.Steps = arr;
  };

  /****************************************************
  Step Object and Functions
  ****************************************************/

  Step = function (frontId, back_step_Id, back_boolean_Id, child_Strat_Id,
      answerId) {
    this.frontId = frontId;
    this.back_step_Id = back_step_Id;
    this.back_boolean_Id = back_boolean_Id;
    this.child_Strat_Id = null;
    this.answerId = answerId;
  };

  Step.prototype.operation = null;
  Step.prototype.isboolean = false;
  Step.prototype.isSpan = false;
  Step.prototype.isSelected = false;
  Step.prototype.isTransform = false;
  Step.prototype.isFiltered = false;
  Step.prototype.isLast = false;
  Step.prototype.prevStepType = "";
  Step.prototype.nextStepType = "";

  /****************************************************
  Utility Functions
  *****************************************************/
    
  getStep = function (strat,id) {
    var i,
        j;

    for (i in wdk.strategy.controller.strats) {
      if (wdk.strategy.controller.strats[i].frontId == strat) {
        for (j=0; j < wdk.strategy.controller.strats[i].Steps.length; j++) {
          if (wdk.strategy.controller.strats[i].Steps[j].frontId == id) {
            return wdk.strategy.controller.strats[i].Steps[j];
          }
        }
      }
    }
    return false;
  };
    
  getStrategy = function (id) {
    var i;

    for (i in wdk.strategy.controller.strats) {
      if (wdk.strategy.controller.strats[i].frontId == id) {
        return wdk.strategy.controller.strats[i];
      }
    }
    return false;
  };

  getSubStrategies = function (id) {
    var substrt,
        arr = [],
        pStrat = getStrategy(id);

    for (substrt in pStrat.Steps) {
      if (pStrat.Steps[substrt].child_Strat_Id) {
        if (getStrategy(pStrat.Steps[substrt].child_Strat_Id) !== false) {
          arr.push(getStrategy(pStrat.Steps[substrt].child_Strat_Id));
        }
      }
    }
    return arr;
  };

  getStrategyFromBackId = function (id) {
    var i;

    for (i in wdk.strategy.controller.strats) {
      if (wdk.strategy.controller.strats[i].backId == id) {
        return wdk.strategy.controller.strats[i];
      }
    }
    return false;
  };

  getStepFromBackId = function (strat,id) {
    var j,
        strategy = getStrategyFromBackId(strat);

    for (j = 0; j < strategy.Steps.length; j++) {
      if (strategy.Steps[j].back_step_Id == id ||
          strategy.Steps[j].back_boolean_Id == id) {
        return strategy.Steps[j];
      }
    }
  };

  findStrategy = function (fId) {
    var i;

    for (i in wdk.strategy.controller.strats) {
      if (wdk.strategy.controller.strats[i].frontId == fId) {
        return i;
      }
    }
    return -1;
  };

  findStep = function (stratId, fId) {
    var i,
        steps = getStrategy(stratId).Steps;

    for (i = 0; i < steps.length; i++) {
      if (steps[i].frontId == fId) {
        return i;
      }
    }
    return -1;
  };

  isLoaded = function (id) {
    var i;

    for (i in wdk.strategy.controller.strats) {
      if (wdk.strategy.controller.strats[i].backId == id) {
        return true;
      }
    } 
    return false;
  };

  getStrategyJSON = function (backId){
    var strategyJSON = null;
    $.ajax({
      async: false,
      url:"showStrategy.do?strategy=" + backId + "&open=false",
      type: "POST",
      dataType: "json",
      data:"pstate=" + wdk.strategy.controller.p_state,
      success: function(data){
        for(var s in data.strategies){
          if(s != "length") {
            data.strategies[s].checksum = s;
            strategyJSON = data.strategies[s];
          }
        }
      }
    });
    return strategyJSON;
  };

  getStrategyOBJ = function (backId){
    if (getStrategyFromBackId(backId) != false) {
      return getStrategyFromBackId(backId);
    } else {
      var json = getStrategyJSON(backId);
      var s = new Strategy(wdk.strategy.controller.strats.length, json.id, false);
      s.checksum = json.checksum;
      s.JSON = json;
      s.name = json.name;
      s.description = json.description;
      return s;
    }
  };

  ns.Strategy = Strategy;
  ns.Step = Step;
  ns.getStrategy = getStrategy;
  ns.getStep = getStep;
  ns.findStep = findStep;
  ns.getStepFromBackId = getStepFromBackId;
  ns.getSubStrategies = getSubStrategies;
  ns.getStrategyFromBackId = getStrategyFromBackId;
  ns.findStrategy = findStrategy;
  ns.isLoaded = isLoaded;
  ns.getStrategyJSON = getStrategyJSON;
  ns.getStrategyOBJ = getStrategyOBJ;

});
