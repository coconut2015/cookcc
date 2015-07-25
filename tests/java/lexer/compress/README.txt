This directory contains tests that check the DFA table compression
routine.  It can also be used to test generated code and see if works
correctly under slightly different variations of the generated tables.

The verification for table compression is enabled with "-lang plain"
option.  Just need to grep the particular line with the message:
# compressed correctly = true
