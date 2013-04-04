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
bumi.analysis=> (g/transact! (count (v/find-by-kv :type "commit")))
362159

bumi.analysis=> (g/transact! (count (v/find-by-kv :type "person")))
16653

bumi.analysis=> (g/transact! (count (v/find-by-kv :type "file")))
73061
```

Note that we can verify that we have the correct number of commits
with the following:

``` bash
git log --pretty=format:'' | wc -l
362158
```

Off by one error! Close. 

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

;; TODO mispelled edited in the codebase. Gotta reload the database
;; now.

```clojure
bumi.analysis=> (g/transact! (q/query (v/refresh linus)
                                      (q/--> :authored :committed)
                                      (q/--> :editted :deleted :created)
                                      q/dedup
                                      q/count!))
70879
```

It seems that the vast majority of files that exist or have ever
existed inside of the Linux kernel have been effected by Linus'
commits at some point. Neat! 