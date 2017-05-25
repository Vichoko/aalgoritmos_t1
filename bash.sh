#!/bin/bash
for i in $(seq 9 21); do
  for d in Uniforme Normal; do
    for a in 0.25 0.5 0.75; do
      java segment/SegmentGenerator $i $d $a > tempout;
      for t in $(seq 1 3); do
        java algorithm/sort/MergeSort $(cat tempout);
      done;
    done;
  done;
done
