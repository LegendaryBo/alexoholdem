package ao.holdem.bots.util;

import ao.holdem.def.model.cards.Hole;
import ao.holdem.def.model.cards.community.Community;

/**
 * 
 */
public interface OddFinder
{
    public Odds compute(Hole      hole,
                        Community community,
                        int       activeOpponents);
}