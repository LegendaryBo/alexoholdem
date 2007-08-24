package ao.decision.domain;

/**
 *
 */
public enum BetsToCall
{
    ZERO, ONE, TWO_PLUS;

    public static BetsToCall fromBets(int bets)
    {
        assert bets >= 0;
        return (bets == 0)
                ? ZERO
                : (bets == 1)
                   ? ONE
                   : TWO_PLUS;
    }
}
