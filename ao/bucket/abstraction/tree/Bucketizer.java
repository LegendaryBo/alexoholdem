package ao.bucket.abstraction.tree;

import ao.bucket.abstraction.tree.BucketTree.Branch;

/**
 * Date: Jan 9, 2009
 * Time: 10:01:28 AM
 */
public interface Bucketizer
{
    public void bucketize(Branch branch,
                          byte   numBuckets);
}
