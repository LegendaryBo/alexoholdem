package ao.bucket.abstraction.bucketize.def;

import ao.bucket.abstraction.access.tree.BucketTree;

/**
 * User: alex
 * Date: 5-Jul-2009
 * Time: 5:32:44 PM
 */
public interface Bucketizer {
    // returns the bucketing error, in some distance metric
    double bucketize(BucketTree.Branch branch,
                     byte              numBuckets);

    String id();
}
