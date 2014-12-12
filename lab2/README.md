# lab2

Simple threadpool-backed crowler.

## Usage

    $ lein run 2 resources/example.in

## Some possible improvements

 + handle relative urls
 + make "http://example.com" and "http://example.com/" indistinguishable
 + agents => lazy seqs
 + add subtree caching (requires cache deepening feature when shorter subtree is
   fetched (or even started being fetched) before cache is hit with response for deeper one)
 + don't fetch files sent in response (handle headers before the data is fetched)
