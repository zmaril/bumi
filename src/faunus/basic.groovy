g = FaunusFactory.open("bin/titan-cassandra.properties")
g.setOutputLocation("output/number-of-committed")
g.V.has("type","person").transform('{it.out("committed").count()}').submit()
hdfs.mergeToLocal("output/number-of-committed/job-0/sideeffect-*","output/number-of-committed/sideeffect")


h = g.getNextGraph()
h.setOutputLocation("output/number-of-authored")
h.V.has("type","person").transform('{it.out("authored").count()}').submit()
hdfs.mergeToLocal("output/number-of-authored/job-0/sideeffect-*","output/number-of-authored/sideeffect")
