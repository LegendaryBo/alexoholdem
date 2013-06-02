package ao.learn.mst.gen3.strategy.impl

import ao.learn.mst.gen3.strategy.{CfrStrategyProfile, CfrStrategyBuffer}

/**
 * 01/06/13 7:39 PM
 */
class MapCfrStrategyBuffer
  extends CfrStrategyBuffer
{
  //--------------------------------------------------------------------------------------------------------------------
  private val reachProbabilityBuffer     = collection.mutable.Map[Int, Double]()
  private val counterfactualRegretBuffer = collection.mutable.Map[Int, Array[Double]]()


  //--------------------------------------------------------------------------------------------------------------------
  def bufferUpdate(informationSetIndex: Int, actionRegret: Seq[Double], reachProbability: Double) {
    val actionCount = actionRegret.length
    initializeInformationSetIfRequired(informationSetIndex, actionCount)

//    val newReachProbability = reachProbabilityBuffer(informationSetIndex) + reachProbability
//    reachProbabilityBuffer.update(informationSetIndex, newReachProbability)
    reachProbabilityBuffer(informationSetIndex) += reachProbability // same as above?

    for (action <- 0 until actionCount) {
      counterfactualRegretBuffer(informationSetIndex)(action) +=
        actionRegret(action) * reachProbability
    }
  }

  private def initializeInformationSetIfRequired(
    informationSetIndex: Int, actionCount: Int)
  {
    if (informationSetIndex < 0) {
      throw new IllegalArgumentException(
        "Index " + informationSetIndex + " must be non-negative.")
    }

    if (! isInformationSetInitialized(informationSetIndex)) {
      reachProbabilityBuffer(informationSetIndex) = 0.0
      counterfactualRegretBuffer(informationSetIndex) = new Array[Double](actionCount)
    } else {
      val currentActionCount = counterfactualRegretBuffer(informationSetIndex).length
      if (currentActionCount != actionCount) {
        throw new IllegalArgumentException("Mismatched action count: " +
          informationSetIndex + " | " + currentActionCount + " vs " + actionCount)
      }
    }
  }

  private def isInformationSetInitialized(informationSetIndex:Int):Boolean =
    reachProbabilityBuffer.contains(informationSetIndex)


  //--------------------------------------------------------------------------------------------------------------------
  def commit(cfrStrategyProfileBuilder: CfrStrategyProfile) {
    for (informationSetIndex:Int <- reachProbabilityBuffer.keys) {
      cfrStrategyProfileBuilder.update(
        informationSetIndex,
        counterfactualRegretBuffer(informationSetIndex),
        reachProbabilityBuffer(informationSetIndex))
    }

    counterfactualRegretBuffer.clear()
    reachProbabilityBuffer.clear()
  }
}
