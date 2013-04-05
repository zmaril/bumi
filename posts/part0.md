Let's start by asking some simple questions about the Linux kernel.
Assuming you've loaded the kernel into Titan with `lein run load`,
then we can switch over into the bumi.analysis namespace and get
started. 

``` clojure
bumi.analysis=> (ns bumi.analysis
  (:require [bumi.titan :refer (start)]
            [clojurewerkz.titanium.graph :as g]
            [clojurewerkz.titanium.vertices :as v]
            [clojurewerkz.titanium.edges :as e]
            [clojurewerkz.titanium.types :as t]
            [ogre.core :as q]))
(start) ;;Start up the database via Titanium. 
```

Let's see how many commits, people, and files we have stored in Titan:

```clojure
bumi.analysis=> (def commits (g/transact! (v/find-by-kv :type "commit")))
bumi.analysis=> (count commits)
362159

bumi.analysis=> (def people (g/transact! (v/find-by-kv :type "person")))
bumi.analysis=> (count people)
16653

bumi.analysis=> (def files (g/transact! (v/find-by-kv :type "file")))
bumi.analysis=> (count files)
73061
```

Note that we can verify that we have the correct number of commits
with the following:

``` bash
git log --pretty=format:'' | wc -l
362158
```

Off by one! Close though. 

Now let's ask some simple questions about Linus. First we must find him.

```clojure
bumi.analysis=> (def linus (g/transact! (first (v/find-by-kv :name "linus torvalds"))))
#'bumi.analysis/linus
bumi.analysis=> linus
#<CacheVertex v[14200]>
bumi.analysis=> (g/transact! (v/to-map (v/refresh linus)))
{:__id__ 14200, :name "linus torvalds", :type "person"}
```

Now, how many commits has Linus authored or committed? 
```clojure
bumi.analysis=> (g/transact! (q/query (v/refresh linus)
                                      (q/--> :authored)
                                      q/count!))
14013
bumi.analysis=> (g/transact! (q/query (v/refresh linus)
                                      (q/--> :committed)
                                      q/count!))
50188
```

So he's been directly involved in about a seventh of all the commits.
I wonder, how many different files has he touched?

```clojure
bumi.analysis=> (g/transact! (q/query (v/refresh linus)
                                      (q/--> :authored :committed)
                                      (q/--> :edited :deleted :created)
                                      q/dedup
                                      q/count!))
70879
```

It seems that the vast majority of files that exist or have ever
existed inside of the Linux kernel have been effected by Linus'
commits at some point. Neat! 

Let's do some sanity checks. Every commit should only have one author
and one committer. We'll define a useful function along the way called
`degree-in` that we'll use to check how many edges with a certain set
of labels are coming into a node. 

```clojure
bumi.analysis=> (defn degree-in [v & labels] (q/query v
                                                      (#(apply q/<-- (cons % labels)))
                                                      q/count!))

bumi.analysis=> (g/transact! (->>  commits
                                   (pmap #(degree-in (v/refresh %) :authored))
                                   frequencies))
{1 362159}

bumi.analysis=> (g/transact! (->>  commits
                                   (pmap #(degree-in (v/refresh %) :committed))
                                   frequencies))
{1 362159}
```

Phew. That makes sense. Every commit only has one author and one
committer. But, I wonder, how many files does the average commit
touch? To answer this, we'll define a useful degree-out function. 

```clojure
bumi.analysis=> (defn degree-out [v & labels]
                                 (q/query v
                                          (#(apply q/--> (cons % labels)))
                                          q/count!))

bumi.analysis=> (g/transact! (->>  commits
                                   (pmap #(degree-out (v/refresh %) :deleted))
                                   frequencies))
....
(clojure.pprint/pprint (sort *1))
([0 350693]
 [1 4961]
 [2 1849]
 [3 597]
 [4 477]
... ;; Lots of numbers. 
 [3195 1]
 [3236 2]
 [3370 1]
 [3692 1]
 [3737 1]
 [4577 1]
 [4678 1])
```

Not so enlightening. We should probably start making these things
pretty, otherwise we will be stuck with some kind of pointless lists
of data. From here on out, we'll stop working with the frequencies of
the degrees and just spit out the list of degrees for R to work with.

First we'll define some useful functions for saving collections to files

``` clojure
bumi.analysis=> (defn seq->string-for-R [col]
                  (apply str (interleave col (cycle ["\n"]))))

bumi.analysis=> (defn spit-seq [filename col]
                  (spit filename (seq->string-for-R col)))
```

Now, we can save all the sequences we are interested in. 

``` clojure
;; Show and use spit seq here and then display R and what not. 

bumi.analysis=> (spit-seq "files-edited-per-commit.txt"
                          (g/transact! (doall (pmap #(degree-out (v/refresh %) :edited) commits))))

bumi.analysis=> (spit-seq "files-created-per-commit.txt"
                          (g/transact! (doall (pmap #(degree-out (v/refresh %) :created) commits))))

bumi.analysis=> (spit-seq "files-deleted-per-commit.txt"
                          (g/transact! (doall (pmap #(degree-out (v/refresh %) :deleted) commits))))


```

And let's whip out some simple R scripts and see what's been going on. 

``` R
> data <- read.table("files-edited-per-commit.txt")
> data <- as.numeric(data$V1)
> summary(data)
    Min.  1st Qu.   Median     Mean  3rd Qu.     Max.
    0.00     1.00     1.00    16.77     3.00 16250.00

> data <- read.table("files-deleted-per-commit.txt")
> data <- as.numeric(data$V1)
> summary(data)
    Min.  1st Qu.   Median     Mean  3rd Qu.     Max.
   0.000    0.000    0.000    1.488    0.000 4678.000

> data <- read.table("files-created-per-commit.txt")
> data <- as.numeric(data$V1)
> summary(data)
     Min.   1st Qu.    Median      Mean   3rd Qu.      Max.
    0.000     0.000     0.000     2.665     0.000 17290.000
``` 

;;TODO code for histogram of editted
png(filename=paste0("files-edited-per-commit-histogram.png"))
histogram <- hist(data,labels=TRUE)
dev.off()

