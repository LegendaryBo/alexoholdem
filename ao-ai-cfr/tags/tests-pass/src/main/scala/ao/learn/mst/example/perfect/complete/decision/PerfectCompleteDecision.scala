package ao.learn.mst.example.perfect.complete.decision

import ao.learn.mst.gen2.game.ExtensiveGameDecision
import ao.learn.mst.gen2.info.InformationSet
import ao.learn.mst.example.perfect.complete.terminal.{PerfectCompleteTerminalDownDown, PerfectCompleteTerminalDownUp, PerfectCompleteTerminalUpDown, PerfectCompleteTerminalUpUp}
import ao.learn.mst.gen2.player.model.{RationalPlayer, NamedFiniteAction, FiniteAction}


//----------------------------------------------------------------------------------------------------------------------
abstract class PerfectCompleteDecision
    extends ExtensiveGameDecision
{
  val actions =
    NamedFiniteAction.sequence(
      "U", "D")
}


//----------------------------------------------------------------------------------------------------------------------
case object PerfectCompleteDecisionFirstInfo extends InformationSet

case object PerfectCompleteDecisionFirst
    extends PerfectCompleteDecision
{
  override def player =
    new RationalPlayer(0)

  def informationSet = PerfectCompleteDecisionFirstInfo

  def child(action: FiniteAction) =
    action.index match {
      case 0 => PerfectCompleteDecisionAfterUp
      case 1 => PerfectCompleteDecisionAfterDown
    }
}


//----------------------------------------------------------------------------------------------------------------------
case object PerfectCompleteDecisionAfterUpInfo extends InformationSet

case object PerfectCompleteDecisionAfterUp
    extends PerfectCompleteDecision
{
  override def player =
    RationalPlayer(1)

  def informationSet = PerfectCompleteDecisionAfterUpInfo

  def child(action: FiniteAction) =
    action.index match {
      case 0 => PerfectCompleteTerminalUpUp
      case 1 => PerfectCompleteTerminalUpDown
    }
}


//----------------------------------------------------------------------------------------------------------------------
case object PerfectCompleteDecisionAfterDownInfo extends InformationSet

case object PerfectCompleteDecisionAfterDown
    extends PerfectCompleteDecision
{
  override def player =
    RationalPlayer(1)

  def informationSet = PerfectCompleteDecisionAfterDownInfo

  def child(action: FiniteAction) =
    action.index match {
      case 0 => PerfectCompleteTerminalDownUp
      case 1 => PerfectCompleteTerminalDownDown
    }
}

