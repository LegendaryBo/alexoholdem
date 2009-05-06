package ao.bucket.index.detail.turn;

import ao.bucket.index.canon.flop.Flop;
import ao.bucket.index.canon.hole.CanonHole;
import ao.bucket.index.canon.river.River;
import ao.bucket.index.canon.turn.Turn;
import ao.bucket.index.canon.turn.TurnLookup;
import ao.bucket.index.detail.CanonDetail;
import ao.bucket.index.detail.flop.FlopDetailFlyweight.CanonFlopDetail;
import ao.bucket.index.detail.flop.FlopDetails;
import ao.bucket.index.detail.turn.TurnDetailFlyweight.CanonTurnDetail;
import ao.bucket.index.enumeration.HandEnum;
import ao.util.io.Dir;
import ao.util.misc.Filter;
import ao.util.misc.Traverser;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Date: Jan 9, 2009
 * Time: 12:39:28 PM
 */
public class TurnDetails
{
    //--------------------------------------------------------------------
    private static final Logger LOG =
            Logger.getLogger(TurnDetails.class);

    private static final File   DIR =
            Dir.get("lookup/canon/detail/turn/");

    private TurnDetails() {}


    //--------------------------------------------------------------------
    private static final TurnDetailFlyweight DETAILS =
            retrieveOrComputeDetails();


    //--------------------------------------------------------------------
    private static TurnDetailFlyweight retrieveOrComputeDetails()
    {
        LOG.debug("retrieveOrComputeDetails");

        TurnDetailFlyweight details =
                TurnDetailFlyweight.retrieve( DIR );
//        computeRiverInfo( details );
//        TurnDetailFlyweight.persist(details, DIR);

        if (details == null)
        {
            details = computeDetails();
            TurnDetailFlyweight.persist(details, DIR);
        }
        return details;
    }


    //--------------------------------------------------------------------
    private static TurnDetailFlyweight computeDetails()
    {
        LOG.debug("computing details");

        final TurnDetailFlyweight fw = new TurnDetailFlyweight();
        HandEnum.uniqueTurns(
                new Traverser<Turn>() {
            public void traverse(Turn turn) {
                int index = turn.canonIndex();
//                if (! fw.isInitiated( index )) {
                    fw.init(turn, TurnOdds.lookup(index));
//                }
//                fw.incrementRepresentation(index);
            }});
        computeRiverInfo( fw );
        return fw;
    }


    //--------------------------------------------------------------------
    private static void computeRiverInfo(
            TurnDetailFlyweight fw)
    {
        LOG.debug("computing river info");

        long   riverOffset = 0;
        byte[] riverCounts = riverCounts();

        for (int i = 0; i < TurnLookup.CANONS; i++)
        {
            fw.setRiverInfo(i, riverOffset/*, riverCounts[ i ]*/);
            riverOffset += riverCounts[ i ];
        }
    }

    public static byte[] riverCounts()
    {
        final byte[] riverCounts =
                new byte[ TurnLookup.CANONS];

        HandEnum.uniqueRivers(new Traverser<River>() {
            public void traverse(River river) {
                riverCounts[ river.turn().canonIndex() ]++;
            }});

        return riverCounts;
    }


    //--------------------------------------------------------------------
    public static CanonTurnDetail lookup(int canonTurn)
    {
        return DETAILS.get(canonTurn);
    }

    public static CanonTurnDetail[] lookup(
            int canonTurnFrom, int canonTurnCount)
    {
        CanonTurnDetail[] details =
                new CanonTurnDetail[ canonTurnCount ];
        for (int i = 0; i < canonTurnCount; i++) {
            details[ i ] = lookup(canonTurnFrom + i);
        }
        return details;
    }

    public static void lookup(
                int canonTurnFrom, int canonTurnCount,
                CanonDetail[] into, int startingAt)
    {
        for (int i = 0; i < canonTurnCount; i++) {
            into[ startingAt + i ] = lookup(canonTurnFrom + i);
        }
    }


    //--------------------------------------------------------------------
    public static List<Turn> examplesOf(int canonTurn)
    {
        final int  acceptTurn = canonTurn;
        CanonFlopDetail flopDetail = FlopDetails.containing(acceptTurn);
        final int  acceptFlop = (int) flopDetail.canonIndex();
        final int  acceptHole = (int) flopDetail.holeDetail().canonIndex();

        final List<Turn> examples = new ArrayList<Turn>();
        HandEnum.turns(
                new Filter<CanonHole>() {
                    public boolean accept(CanonHole canonHole) {
                        return canonHole.canonIndex() == acceptHole;
                    }
                },
                new Filter<Flop>() {
                    public boolean accept(Flop flop) {
                        return flop.canonIndex() == acceptFlop;
                    }
                },
                new Filter<Turn>() {
                    public boolean accept(Turn turn) {
                        return turn.canonIndex() == acceptTurn;
                    }
                },
                new Traverser<Turn>() {
            public void traverse(Turn turn) {
                examples.add( turn );
            }
        });
        return examples;
    }
}
