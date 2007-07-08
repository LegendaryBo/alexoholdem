package ao.holdem.def.model.card.eval7;

import ao.holdem.def.model.card.Card;
import ao.holdem.def.model.card.eval_567.EvalSlow;
import ao.holdem.def.model.card.lookup.PersistentInts;
import ao.holdem.def.model.card.lookup.PersistentLongs;
import ao.holdem.def.model.cards.Hand;

import java.util.Arrays;

/**
 * http://forumserver.twoplustwo.com/showflat.php?Cat=0&Number=8513906&page=0&fpart=all&vc=1
 */
public class Eval7Faster
{
	private final static int NUM_SUITS = 4;
	private final static int NUM_RANKS = 13;

	private static int[]   handRanks = null;        // array to hold hand rank lookup table
	private static boolean verbose   = true;		// toggles verbose mode

	private static int[]  hand; 					// re-usable array to hold cards in a hand
	private static long[] keys             = null;	// array to hold key lookup table
	private static int    numKeys          = 1;		// counter for number of defined keys in key array
	private static long   maxKey           = 0;		// holds current maximum key value
	private static int    numCards         = 0;		// re-usable counter for number of cards in a hand
	private static int    cardIndex        = 0;		// re-usable index for cards in a hands

    private static long   startTimer;
	private static long   stopTimer;


    //--------------------------------------------------------------------
    static
    {
        handRanks = PersistentInts.retrieve("lookup/fast_ranks.cache");
        keys      = PersistentLongs.retrieve("lookup/fast_keys.cache");

        if (handRanks == null || keys == null)
        {
            handRanks = new int[32487834];
            keys      = new long[612978];

            generateTables();

            PersistentInts.persist(handRanks, "lookup/fast_ranks.cache");
            PersistentLongs.persist(keys,     "lookup/fast_keys.cache");
        }
    }


    //--------------------------------------------------------------------
    // Inserts a key into the key array and returns the insertion index.
	private static int insertKey(long key) {
		// check to see if key is valid
		if (key == 0) {
			return 0;
		}

		// short circuit insertion for most common cases
		if (key >= maxKey) {
			if (key > maxKey) {
				keys[numKeys++] = key;	// appends the new key to the key array
				maxKey = key;
			}
			return numKeys - 1;
		}

		// use binary search to find insertion point for new key
		int low = -1;
		int high = numKeys;
		int pivot;
		long difference;

		while (high - low > 1) {
			pivot = (low + high) >>> 1;
			difference = keys[pivot] - key;
			if (difference > 0) {
				high = pivot;
			}
			else if (difference < 0) {
				low = pivot;
			}
			else {
				return pivot;	// key already exists
			}
		}

		// key does not exist so must be inserted
		System.arraycopy(keys, high, keys, high + 1, numKeys - high);
		keys[high] = key;

		numKeys++;
		return high;
	}


    //--------------------------------------------------------------------
	// Returns a key for the hand created by adding a new card to the hand
	// represented by the given key.  Returns 0 if new card already appears in hand.
	private static long makeKey(long baseKey, int newCard) {

		int[] suitCount = new int[NUM_SUITS + 1]; 	// number of times a suit appears in a hand
		int[] rankCount = new int[NUM_RANKS + 1];	// number of times a rank appears in a hand
		hand = new int[8];

		// extract the hand represented by the key value
		for (cardIndex = 0; cardIndex < 6; cardIndex++) {

			// hand[0] is used to hold the new card
			hand[cardIndex + 1] = (int)((baseKey >>> (8 * cardIndex)) & 0xFF);
		}

		hand[0] = formatCard8bit(newCard);

		// examine the hand to determine number of cards and rank/suit counts
		for (numCards = 0; hand[numCards] != 0; numCards++) {
			suitCount[hand[numCards] & 0xF]++;
			rankCount[(hand[numCards] >>> 4) & 0xF]++;

			// check to see if new card is already contained in hand (rank and suit considered)
			if (numCards != 0 && hand[0] == hand[numCards]) {
				return 0;
			}
		}

		// check to see if we already have four of a particular rank
		if (numCards > 4) {
			for (int rank = 1; rank < 14; rank++) {
				if (rankCount[rank] > 4) return 0;
			}
		}

		// determine the minimum number of suits required for a flush to be possible
		int minSuitCount = numCards - 2;

		// check to see if suit is significant
		if (minSuitCount > 1) {
			// examine each card in the hand
			for (cardIndex = 0; cardIndex < numCards; cardIndex++) {
				// if the suit is not significant then strip it from the card
				if (suitCount[hand[cardIndex] & 0xF] < minSuitCount) {
					hand[cardIndex] &= 0xF0;
				}
			}
		}

		sortHand();

		long key = 0;
		for (int i = 0; i < 7; i++) {
			key += (long)hand[i] << (i * 8);
		}

		return key;
	}


    //--------------------------------------------------------------------
	// Formats and returns a card in 8-bit packed representation.
	private static int formatCard8bit(int card) {

        // 8-Bit Packed Card Representation
        // +--------+
        // |rrrr--ss|
        // +--------+
        // r = rank of card		(deuce = 1, trey = 2, four = 3, five = 4,..., ace = 13)
		// s = suit of card		(suits are arbitrary, can take value from 0 to 3)

		card--;
		return (((card >>> 2) + 1) << 4) + (card & 3) + 1;
	}


    //--------------------------------------------------------------------
	// Sorts the hand using Bose-Nelson Sorting Algorithm (N = 7).
	private static void sortHand() {
		swapCard(0, 4);
		swapCard(1, 5);
		swapCard(2, 6);
		swapCard(0, 2);
		swapCard(1, 3);
		swapCard(4, 6);
		swapCard(2, 4);
		swapCard(3, 5);
		swapCard(0, 1);
		swapCard(2, 3);
		swapCard(4, 5);
		swapCard(1, 4);
		swapCard(3, 6);
		swapCard(1, 2);
		swapCard(3, 4);
		swapCard(5, 6);
	} // End sortHand method


	// Swaps card i with card j.
	private static void swapCard(int i, int j) {
		if (hand[i] < hand[j]) {
			hand[i] ^= hand[j];
			hand[j] ^= hand[i];
			hand[i] ^= hand[j];
		}
	}


    //--------------------------------------------------------------------
	// Determines the relative strength of a hand (the hand is given by its unique key value).
	private static int getHandRank(long key) {

		// The following method implements a modified version of "Cactus Kev's Five-Card
		// Poker Hand Evaluator" to determine the relative strength of two five-card hands.
		// Reference: http://www.suffecool.net/poker/evaluator.html

		hand = new int[8];
		int currentCard;
		int rank;
        int handRank = -1;
		int suit;
		int numCards = 0;

        Card checkup[] = new Card[7];
        final int[] primes = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41};

        if (key != 0) {
            int lastDontCare = 1;
            for (cardIndex = 0; cardIndex < 7; cardIndex++) {
            	currentCard = (int)((key >>> (8 * cardIndex)) & 0xFF);

                if (currentCard == 0) break;
                numCards++;

                // Cactus Kev Card Representation
                // +--------+--------+--------+--------+
                // |xxxbbbbb|bbbbbbbb|cdhsrrrr|xxpppppp|
                // +--------+--------+--------+--------+
                // p    = prime number of rank (deuce = 2, trey = 3, four = 5, five = 7,..., ace = 41)
                // r    = rank of card         (deuce = 0, trey = 1, four = 2, five = 3,..., ace = 12)
                // cdhs = suit of card
                // b    = bit turned on depending on rank of card

                // extract suit and rank from 8-bit packed representation
                rank = (currentCard >>> 4) - 1; // 0..12
                suit = currentCard & 0xF; //0..4, 0 = don't care.

                if (suit == 0)
                {
                    do
                    {
                        checkup[cardIndex] =
                                Card.values()[ (lastDontCare - 1)*13 + rank ];

                        lastDontCare = (lastDontCare + 1) % 5;
                        if (lastDontCare == 0) lastDontCare = 1;
                    }
                    while (contains(checkup, cardIndex - 1, checkup[cardIndex]));
                }
                else
                {
                    checkup[cardIndex] =
                            Card.values()[ (suit - 1)*13 + rank ];
                }

                // change card representation to Cactus Kev Representation
                hand[cardIndex] = primes[rank] | (rank << 8) | (1 << (suit + 11)) | (1 << (16 + rank));
            }

            switch (numCards) {
                case 5 :
                    handRank = EvalSlow.valueOf(
                            hand[0],hand[1],hand[2],hand[3],hand[4]);
//                    if (handRank !=
//                            EvalSlow.valueOf(
//                                    checkup[0],checkup[1],checkup[2],
//                                    checkup[3],checkup[4]))
//                    {
//                        System.out.println("### WTF?!!??!");
//                    }

                    break;

                case 6 :
                    handRank = EvalSlow.valueOf(
                            hand[0],hand[1],hand[2],hand[3],hand[4],hand[5]);
//                    handRank = EvalSlow.valueOf(
//                            checkup[0],checkup[1],checkup[2],
//                            checkup[3],checkup[4],checkup[5]);
                    break;

                case 7 :
//                    handRank =
//                            EvalSlow.valueOf(
//                                    checkup[0],checkup[1],checkup[2],
//                                    checkup[3],checkup[4],checkup[5],
//                                    checkup[6]);

                    handRank = EvalSlow.valueOf(
                            hand[0],hand[1],hand[2],hand[3],hand[4],hand[5],hand[6]);
//                    if (handRank !=
//                            EvalSlow.valueOf(
//                                    checkup[0],checkup[1],checkup[2],
//                                    checkup[3],checkup[4],checkup[5],
//                                    checkup[6]))
//                    {
//                        System.out.println("### WTF?!!??!");
//                    }

                    break;

                default :
                    System.out.println("ERROR: Invalid hand in GetRank method.");
                    break;
            }
        }
        return handRank;
	}

    private static boolean contains(Card cards[], int upToIndex, Card card)
    {
        for (int i = 0; i <= upToIndex; i++)
        {
            if (cards[i] == card)
            {
                return true;
            }
        }
        return false;
    }

		
    //--------------------------------------------------------------------
	private static void generateTables() {

	        int  handRank;
	        int  keyIndex;
	        long key;

	        if (verbose) {
	        	System.out.print("\nGenerating and sorting keys...");
	        	startTimer = System.currentTimeMillis();
	        }

	        for (keyIndex = 0; keys[keyIndex] != 0 || keyIndex == 0; keyIndex++) {
//                if (keyIndex % 1000 == 0) System.out.println("key: " + keyIndex);

                for (int card = 1; card < 53; card++) {	      		// add a card to each previously calculated key
	            	key = makeKey(keys[keyIndex], card);  			// create the new key

	                if (numCards < 7) insertKey(key);				// insert the new key into the key lookup table
	            }
	        }

	        if (verbose) {
	        	stopTimer = System.currentTimeMillis();
	        	System.out.printf("done.\n\n%35s %d\n", "Number of Keys Generated:", (keyIndex + 1));
	        	System.out.printf("%35s %f seconds\n\n", "Time Required:", ((stopTimer - startTimer) / 1000.0));
		        System.out.print("Generating hand ranks...");
		        startTimer = System.currentTimeMillis();
	        }

	        for (keyIndex = 0; keys[keyIndex] != 0 || keyIndex == 0; keyIndex++) {

	            for (int card = 1; card < 53; card++) {
	            	key = makeKey(keys[keyIndex], card);

	                if (numCards < 7) {
	                	handRank = insertKey(key) * 53 + 53;  		// if number of cards is < 7 insert key
	                }
	                else {
	                	handRank = getHandRank(key);   				// if number of cards is 7 insert hand rank
	                }

                    int maxHandRankIndex = keyIndex * 53 + card + 53;
	                handRanks[maxHandRankIndex] = handRank;			// populate hand rank lookup table with appropriate value
	            }

	            if (numCards == 6 || numCards == 7) {
	            	// insert the hand rank into the hand rank lookup table
	            	handRanks[keyIndex * 53 + 53] = getHandRank(keys[keyIndex]);
	            }
	        }

	        if (verbose) {
	        	stopTimer = System.currentTimeMillis();
	        	System.out.printf("done.\n\n%35s %f seconds\n\n", "Time Required:", ((stopTimer - startTimer) / 1000.0));
	        }
	}


    //--------------------------------------------------------------------
    public static short valueOf(Card... sevenCards)
    {
        return valueOf(sevenCards[0], sevenCards[1], sevenCards[2],
                         sevenCards[3], sevenCards[4], sevenCards[5],
                         sevenCards[6]);
    }
    public static short valueOf(
            Card c1, Card c2, Card c3, Card c4,
            Card c5, Card c6, Card c7)
    {
        return valueOf(c1.index(), c2.index(), c3.index(), c4.index(),
                         c5.index(), c6.index(), c7.index());
    }
    public static short valueOf(
            int index1, int index2, int index3, int index4,
            int index5, int index6, int index7)
    {
//        HR(HR(HR(HR(c1 * 140660 - c2 * 2704 + c3 * 52 - c4) + c5) + c6) + c7)

        return (short) handRanks[handRanks[handRanks[handRanks[
                handRanks[handRanks[handRanks[
                    53 + index1] + index2] + index3] + index4] +
                           index5] + index6] + index7];
//        return (short) handRanks[handRanks[handRanks[handRanks[
//                index1 * 140660 - index2 * 2704 + index3 * 52 - index4] +
//                           index5] + index6] + index7];
    }

//    public static int finalIndexOf(
//            int index1, int index2, int index3, int index4,
//            int index5, int index6, int index7)
//    {
//        return handRanks[handRanks[handRanks[
//                handRanks[handRanks[handRanks[
//                    53 + index1] + index2] + index3] + index4] +
//                           index5] + index6] + index7;
//    }

    //--------------------------------------------------------------------
	public static void main(String [] args) {
//        generateTables();
//        PersistentInts.persist(handRanks, "lookup/fast_ranks.cache");
//        PersistentLongs.persist(keys,     "lookup/fast_keys.cache");
        
        int c0, c1, c2, c3, c4, c5, c6;
        int u0, u1, u2, u3, u4, u5;
//		int numHands = 0;
//		int handRank;
//        int[]          handEnumerations = new int[10];
//        int[][] equivalencyEnumerations = new int[10][3000];
//        String[] handDescriptions = {"Invalid Hand", "High Card", "One Pair", "Two Pair", "Three of a Kind",
//        		                     "Straight", "Flush", "Full House", "Four of a Kind", "Straight Flush"};

        if (verbose) {
        	System.out.print("Enumerating hand frequencies and equivalency classes...");
        	startTimer = System.currentTimeMillis();
        }
        int frequency[] = new int[ Hand.HandRank.values().length ];

        for (c0 = 1; c0 < 53; c0++) {
            u0 = handRanks[53 + c0];
            for (c1 = c0 + 1; c1 < 53; c1++) {
                u1 = handRanks[u0 + c1];
                for (c2 = c1 + 1; c2 < 53; c2++) {
                    u2 = handRanks[u1 + c2];
                    for (c3 = c2 + 1; c3 < 53; c3++) {
                        u3 = handRanks[u2 + c3];
                        for (c4 = c3 + 1; c4 < 53; c4++) {
                            u4 = handRanks[u3 + c4];
                            for (c5 = c4 + 1; c5 < 53; c5++) {
                                u5 = handRanks[u4 + c5];
                                for (c6 = c5 + 1; c6 < 53; c6++) {

//                                    handRank = handRanks[u5 + c6];

//                                    handRank = handRanks[handRanks[handRanks[handRanks[handRanks[handRanks[handRanks[
//                                            53 + c0] + c1] + c2] + c3] + c4] + c5] + c6];

//                                    int value = valueOf(c0, c1, c2, c3, c4, c5, c6);
                                    int value = handRanks[u5 + c6];
                                    frequency[ Hand.HandRank.fromValue(value).ordinal() ]++;

//                                    handEnumerations[handRank >>> 12]++;
//                                	equivalencyEnumerations[handRank >>> 12][handRank & 0xFFF]++;
//                                	numHands++;
                                }
                            }
                        }
                    }
                }
            }
        }

        if (verbose) {
        	stopTimer = System.currentTimeMillis();
        	System.out.printf("done.\n\n%35s %f seconds\n\n", "Time Required:", ((stopTimer - startTimer) / 1000.0));
        }

        System.out.println(Arrays.toString(frequency));

//        System.out.println("SEVEN-CARD POKER HAND FREQUENCIES AND EQUIVALENCY CLASSES\n");
//        System.out.printf("    %-17s %15s %15s\n", "HAND", "FREQUENCY", "CLASSES");
//        System.out.println("    -------------------------------------------------");
//
//        int sumEquivalency;
//        int numClasses = 0;
//        for (int i = handEnumerations.length - 1; i >= 0; i--) {
//        	sumEquivalency = 0;
//        	for (int j = 0; j < equivalencyEnumerations[i].length; j++) {
//        		if (equivalencyEnumerations[i][j] != 0) sumEquivalency++;
//        	}
//        	numClasses += sumEquivalency;
//        	System.out.printf("    %-17s %15d %15d\n", handDescriptions[i], handEnumerations[i], sumEquivalency);
//        }
//        System.out.println("    -------------------------------------------------");
//        System.out.printf("    %-17s %15d %15d\n", "TOTAL", numHands, numClasses);
	}

}