/**
 *  WDK Strategy module
 *
 *  This is using the API defined at https://github.com/semmypurewal/jermaine/blob/master/lib/util/util.js.
 *
 */

/**
 *  Our namespace is located at org.gusdb.wdk.strategy
 *  `exports` is a reference to the namespace, which means
 *  adding thing to it will make them publicly available.
 *  Remember, a namespace is essentially an object.
 *
 *  Scope of work here:
 *  - Scope all variables (using jshint for globals reporting).
 *  - Attach functions and objects to namespace
 *  - Pass JSHint (TODO - determine "WDK profile")
 */
wdk.util.namespace("wdk.strategy", function(exports) {
  "use strict";

  var strats = {},
      sidIndex = 0;

  // The following lines were taken from model-JSON.js

  /**************************************************
  Strategy Object and Functions
  ****************************************************/

  function Strategy(frontId, backId, isDisplay){
    this.frontId = frontId;
    this.backId = backId;
    this.isDisplay = isDisplay;
    this.subStratOrder = {};
  }
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

  Strategy.prototype.getStep = function(stepId,isfront){
    for(var s in this.Steps){
      if(isfront){
        if(this.Steps[s].frontId == stepId)
          return this.Steps[s];
      }else{
        if(this.Steps[s].back_step_Id == stepId || this.Steps[s].back_boolean_Id == stepId)
          return this.Steps[s];
      }
    }
    return null;
  };

  Strategy.prototype.findParentStep = function(stpId, isFront){
    if(this.subStratOf == null){
      return null;
    }else{
      return getStrategy(this.subStratOf).findStep(stpId, isFront);
    }
  };

  Strategy.prototype.findStep = function(stpId, isFront){
    var s = null;
    var st = this.getStep(stpId,isFront);
    if(st){
      s = {str:this, stp:st};
    }else{
      for(var t in this.subStratOrder){
        s = getStrategy(this.subStratOrder[t]).findStep(stpId, isFront);
        if(s != null) break;
      }
    }
    return s;
  };

  Strategy.prototype.getLastStep = function(){
    var cId = 0;
    for(var s in this.Steps){
      cId = this.Steps[s].frontId > cId?this.Steps[s].frontId:cId;
    }
    return this.getStep(cId,true);
  };

  Strategy.prototype.depth = function(stepid, d){
    if(this.subStratOf == null)
      return 0;
    var parStrat = this;
    if(stepid == null){
      d = 1;
      var ssParts = this.backId.split("_");
      stepid = ssParts[1];
      parStrat = getStrategyFromBackId(ssParts[0]);
    }
    var parStep = parStrat.getStep(stepid, false);
    if(parStep != null){
      return 1;
    }else{
      var subS = getSubStrategies(parStrat.frontId);
      if(subS.length == 0)
        return null;
      for(var str in subS){
        var r = subS[str].depth(stepid, d);
        if(r != null)
          return d + r;
      }
      
    }
  };

  Strategy.prototype.initSteps = function(steps, ord){
    var arr = [];
    var st = null;
    var ssind = 1;
    var stepCount = steps.length;
    for(var i in steps){
      if(i != "length" && i != "nonTransformLength"){
        if(steps[i].step != null){
          st = new Step(i, steps[i].step.id, steps[i].id, null, steps[i].step.answerId);
          st.operation = steps[i].operation;
          if(steps[i].isboolean){ 
            st.isboolean = true;
            st.isSpan = false;
          }else{
            st.isSpan = true;
            st.isboolean = false;
                                          st.operation = "SPAN";
          }
          if(steps[i].step.isCollapsed && steps[i].step.strategy.order > 0){
            // TODO - find out what sidIndex is for
            this.subStratOrder[steps[i].step.strategy.order] = sidIndex;
            // TODO - loadModel is defined in controller-JSON.. is this correct?
            var subId = loadModel(steps[i].step.strategy, ord + "." + ssind);
            ssind++;
            this.subStratOrder[steps[i].step.strategy.order] = subId;
            st.child_Strat_Id = subId;
          }
        }else{ 
          st = new Step(i, steps[i].id, "", null, steps[i].answerId);
          if(steps[i].istransform){
            st.isTransform = true;
          }
        }
        if(i == stepCount)
          st.isLast = true;
        if(st.frontId != 1){
          var pstp = steps[parseInt(i, 10)-1];
          if(parseInt(i, 10)-1 == 1)
            st.prevStepType = "";
          else 
            st.prevStepType = (pstp.istransform) ? "transform" : "boolean";
        }
        if(!st.isLast){
          var nstp = steps[parseInt(i, 10)+1];
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

  function Step(frontId, back_step_Id, back_boolean_Id, child_Strat_Id, answerId){
    this.frontId = frontId;
    this.back_step_Id = back_step_Id;
    this.back_boolean_Id = back_boolean_Id;
    this.child_Strat_Id = null;
    this.answerId = answerId;
  }
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
    
  function getStep(strat,id){
    for(var i in strats){
      if(strats[i].frontId == strat){
        for(var j=0;j<strats[i].Steps.length;j++){
          if(strats[i].Steps[j].frontId == id)
            return strats[i].Steps[j];
        }
      }
    }
    return false;
  }
    
  function getStrategy(id){
    for(var i in strats){
      if(strats[i].frontId == id)
        return strats[i];
    }
    return false;
  }

  function getSubStrategies(id){
    var arr = [];
    var pStrat = getStrategy(id);
    for(var substrt in pStrat.Steps){
      if(pStrat.Steps[substrt].child_Strat_Id != null){
        if(getStrategy(pStrat.Steps[substrt].child_Strat_Id) != false)
          arr.push(getStrategy(pStrat.Steps[substrt].child_Strat_Id));
      }
    }
    return arr;
  }

  function getStrategyFromBackId(id){
    for(var i in strats){
      if(strats[i].backId == id)
        return strats[i];
    }
    return false;
  }

  function getStepFromBackId(strat,id){
    var strategy = getStrategyFromBackId(strat);
    for(var j=0;j<strategy.Steps.length;j++){
      if(strategy.Steps[j].back_step_Id == id ||
          strategy.Steps[j].back_boolean_Id == id)
        return strategy.Steps[j];
    }
  }

  function findStrategy(fId){
    for(var i in strats){
      if(strats[i].frontId == fId)
        return i;
    }
    return -1;
  }

  function findStep(stratId, fId){
    var steps = getStrategy(stratId).Steps;
    for(var i=0;i<steps.length;i++){
      if(steps[i].frontId == fId)
        return i;
    }
    return -1;
  }

  function isLoaded(id){
    for(var i in strats){
      if(strats[i].backId == id)
        return true;
    } 
    return false;
  }	

  // from controller-JSON.js, slightly modified
  // needed to run tests...
  function loadModel(json, ord, state){
    var strategy = json;
    var strat = null;
    if(!isLoaded(strategy.id)){
      var strat = new Strategy(sidIndex, strategy.id, true);
      sidIndex++;
    }else{
      var strat = getStrategyFromBackId(strategy.id);
      strat.subStratOrder = new Object();
    }   
    if(strategy.importId != ""){
      strat.isDisplay = true;
      strat.checksum = state[ord].checksum;
    }else{
      var prts = strat.backId.split("_");
      strat.subStratOf = getStrategyFromBackId(prts[0]).frontId;
      if(strategy.order > 0){ 
        strat.isDisplay = true;
      }   
    }
    strat.JSON = strategy;
    strat.isSaved = strategy.saved;
    strat.name = strategy.name;
    strat.description = strategy.description;
    strat.importId = strategy.importId;
    var steps = strategy.steps;
    strats[ord] = strat;
    strat.initSteps(steps, ord);
    strat.dataType = strategy.steps[strategy.steps.length].dataType;
    strat.displayType = strategy.steps[strategy.steps.length].displayType;
    strat.nonTransformLength = strategy.steps.nonTransformLength;
    //strat.DIV = displayModel(strat);
    return strat.frontId;
  }
  

  exports.Strategy = Strategy;
  exports.Step = Step;
  exports.strats = strats;
  exports.loadModel = loadModel;
    
});
