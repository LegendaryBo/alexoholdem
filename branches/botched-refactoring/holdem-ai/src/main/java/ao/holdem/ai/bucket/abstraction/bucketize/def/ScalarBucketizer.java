package ao.holdem.ai.bucket.abstraction.bucketize.def;

import ao.holdem.ai.bucket.abstraction.access.tree.BucketTree.Branch;
import ao.holdem.ai.bucket.abstraction.bucketize.linear.IndexedStrengthList;

/**
 * Date: Jan 9, 2009
 * Time: 10:01:28 AM
 */
public interface ScalarBucketizer extends Bucketizer
{
    // returns the error
    public double bucketize(Branch              branch,
                            IndexedStrengthList details,
                            int                 numBuckets);
}