package com.nlaw.leadDedupe;

import java.util.List;

/**
 *  DedupeService
 *
 *  Provides methods for the deduplication of a list of items.
 *
 *  Only exposes one public method, since we only need to let the outside world
 *  give us a list of things to deduplicate. This means most of its logic will
 *  be contained in private helper methods.  While this isn't ideal from a
 *  testing perspective, this will simplify the implementation a bit by allowing
 *  us to avoid passing a bunch of data structures around and avoids exposing
 *  methods that wouldn't really have context elsewhere.
 *
 *  @author nlawrence
 *
 */
public interface DedupeService {

    List<Lead> deduplicateItems(String inputFilename);

}
