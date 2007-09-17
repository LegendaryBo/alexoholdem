package ao.holdem.def.model.cards;

import ao.holdem.def.model.card.Card;
import ao.util.rand.Rand;

/**
 * Random deck of cards.
 */
public class Deck
{
    //--------------------------------------------------------------------
    private final Card cards[];
    private       int  nextIndex = 0;


    //--------------------------------------------------------------------
    public Deck()
    {
        cards = Card.values().clone();

//        // Shuffle cards
//        for (int i = cards.length; i > 1; i--)
//        {
//            swap(cards, i-1, Rand.nextInt(i));
//        }
    }

    private Deck(Card copyCards[], int copyNextIndex)
    {
        cards     = copyCards;
        nextIndex = copyNextIndex;
    }


    //--------------------------------------------------------------------
    public Hole nextHole()
    {
        return new Hole(nextCard(), nextCard());
    }


    //--------------------------------------------------------------------
    public Community nextFlop()
    {
        return new Community(nextCard(), nextCard(), nextCard());
    }


    //--------------------------------------------------------------------
    public Card nextCard()
    {
        assert nextIndex < cards.length;

        int swapRange = cards.length - nextIndex;
        int destIndex = swapRange - 1;
        swap(cards, destIndex, Rand.nextInt(swapRange));

        nextIndex++;
        return cards[ destIndex ];
    }


    //--------------------------------------------------------------------
    public Deck prototype()
    {
        return new Deck(cards.clone(), nextIndex);
    }


    //--------------------------------------------------------------------
    private static void swap(Card[] arr, int i, int j)
    {
        Card tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }
}