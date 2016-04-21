# Evaluation

The evaluation was done on a data set generated under the following
conditions:

- Seven storage servers running on two hosts.
- Four clients performing the same 1000 requests each.
- Four values for the write/read quorums were used. I varied Write from 4 to 7
  and set Read equal to `(8 - Write)`. That is the minimum Read such that
  `(Read + Write) > N`, and there's no point in raising Read past the minimum.
  - Write = 4, Read = 4
  - Write = 5, Read = 3
  - Write = 6, Read = 2
  - Write = 7, Read = 1
- Nine values were used for the ratio of write to read requests. I set the
  total percentage of write requests to 10%, 20%, 30%, 40%, 50%, 60%, 70%,
  80%, and 90%.
- 4 Quorum Sizes * 9 Read/Write Ratios = 36 Data points total for read latency
  and write latency each.

## Plots

I plotted the median read latencies and median write latencies on two graphs,
which are below.

![Median read latencies](readLatency.png)

![Median write latencies](writeLatency.png)


## Conclusion

On read-heavy workloads, choosing a high write quorum and low read quorum 
cuts off about 4 ms per read, but adds about 4 ms to each write. As expected,
there is a linear progression between data points on the 10% write load.

As the write load increases, the read AND write latencies of the 7-1 config
rises rapidly. However, write latencies on the other configs stay roughly
constant. Read latencies do grow, but not nearly as much as the 7-1.

The reason that 7-1 causes such latency is probably because all servers have
their internal write lock locked nearly constantly. And since the version
number on the file is constantly rising, there is a great deal more syncing
happening, which puts the write lock under even more stress. Adding the fact
that each individual write takes longer because there are more servers to
write to, and its not surprising the 7-1 config is so slow under write-heavy
loads.

The config most in favor of writes, 4-4, is becomes the best at read latency
when write percentage is > 50%. It is becomes the best at write latency
earlier, at > 30%.

Overall, for extremely read-heavy loads (write percentage < 25%), 7-1 is the
best config, as it minimizes read latency and write latencies stay low
regardless of config. If write percentage > 50%, 4-4 is the best config, as
the read latency is still low and write latency is minimized. When write
percentage is between 25% and 50%, 5-3 and 6-2 are the best options, depending
on what the workload is like and how much the requests take in total.
