### FP. Lab #1

Simple data clustering app.

Run with `lein run FILENAME DIST` where

  * `FILENAME` is a valid file name (e.g. `samples/test.txt`).
  The file should contain csv data.
  The last value in a row is assumed to be a label.

  * `DIST` is one of
    * `eucl` for euclidean distance
    * `hamm` for Hamming distance

  The `ra` value (`src/lab1/core.clj:31`) is currently set
  to pass the minimal test case (`3`).
  `ra` for bezdekIris is `2`.
