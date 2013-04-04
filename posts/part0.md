Let's start by asking some simple questions about the Linux kernel.
Assuming you've loaded the kernel into Titan with `lein run load`,
then we can switch over into the bumi.analysis namespace and get
started. 

``` clojure
(ns bumi.analysis) ;;Switch namespaces
(start) ;;Start up the database via Titanium. 
```

Let's see how many commits, people, and files we have stored in Titan:

```clojure
(g/transact! (count (v/find-by-kv :type "commit")))

(g/transact! (count (v/find-by-kv :type "person")))

(g/transact! (count (v/find-by-kv :type "file")))


```
