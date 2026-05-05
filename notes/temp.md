
## ohne Import der Matches in DB

Kernaussage: parallel lohnt sich nicht

### climate_500
- parallel
    - Sat Jul 15 14:22:56 CEST 2023: Matching 287921 objects in 3283 blocks (62.574.464 comparisons) [100%]
    - generateInitialClustering > generateAutoMatching > Time is 27,1 seconds
- sequential
    - Sat Jul 15 14:26:10 CEST 2023: Matching 287921 objects in 3283 blocks (62.574.464 comparisons) [99%]
    - generateInitialClustering > generateAutoMatching > Time is 34,6 seconds
- sequential mit INSERT
    - Free memory (bytes): 297380760
    - Sat Jul 15 17:49:49 CEST 2023: Matching 287921 objects in 3283 blocks (62.574.464 comparisons) [99%]
    - generateInitialClustering > generateAutoMatching > Time is 48,0 seconds

### climate_1mio

- parallel
    - Sat Jul 15 14:35:56 CEST 2023: Matching 513398 objects in 3773 blocks (196.694.240 comparisons) [99%]
    - generateInitialClustering > generateAutoMatching > Time is 95,1 seconds
- sequential
    - Sat Jul 15 14:33:04 CEST 2023: Matching 513398 objects in 3773 blocks (196.694.240 comparisons) [99%]
    - generateInitialClustering > generateAutoMatching > Time is 87,3 seconds
- sequential mit INSERT
    - Sat Jul 15 18:01:40 CEST 2023: Matching 513398 objects in 3773 blocks (196.694.240 comparisons) [99%]
    - generateInitialClustering > generateAutoMatching > Time is 145,1 seconds

### climate_quarter

- parallel
    - Sat Jul 15 15:40:36 CEST 2023: Matching 1325510 objects in 5077 blocks (1.204.194.810 comparisons) [100%]
    - generateInitialClustering > generateAutoMatching > Time is 607,1 seconds
- sequential
    - Sat Jul 15 15:04:59 CEST 2023: Matching 1325510 objects in 5077 blocks (1.204.194.810 comparisons) [100%]
    - generateInitialClustering > generateAutoMatching > Time is 609,5 seconds
- sequential mit INSERT
    - Sat Jul 15 18:23:22 CEST 2023: Matching 1325510 objects in 5077 blocks (1.204.194.810 comparisons) [100%]
    - generateInitialClustering > generateAutoMatching > Time is 867,1 seconds


### climate_half

- parallel
    - Sat Jul 15 17:22:30 CEST 2023: Matching 2356366 objects in 5851 blocks (3.888.278.349 comparisons) [100%]
    - generateInitialClustering > generateAutoMatching > Time is 2101,9 seconds
- sequential
    - Sat Jul 15 16:43:36 CEST 2023: Matching 2356366 objects in 5851 blocks (3.888.278.349 comparisons) [100%]
    - generateInitialClustering > generateAutoMatching > Time is 1932,0 seconds
- sequential mit INSERT
    - Sat Jul 15 21:12:02 CEST 2023: Matching 2356366 objects in 5851 blocks (3.888.278.349 comparisons) [100%]
    - generateInitialClustering > generateAutoMatching > Time is 2682,4 seconds

    