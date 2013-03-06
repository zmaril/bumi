bumi_dir = System.getenv().BUMI_CURR

g = FaunusFactory.open(bumi_dir+"/resources/seqfile.properties")
g.setInputLocation(bumi_dir+"/output/seqfile/job-0")


g.setOutputLocation(bumi_dir+"/output/number-of-committed")
g.V.has("type","person").transform('{it.out("committed").count()}').submit()
hdfs.mergeToLocal(bumi_dir+"/output/number-of-committed/job-0/sideeffect-*",bumi_dir+"/output/number-of-committed/sideeffect")


g.setOutputLocation(bumi_dir+"/output/number-of-authored")
g.V.has("type","person").transform('{it.out("authored").count()}').submit()
hdfs.mergeToLocal(bumi_dir+"/output/number-of-authored/job-0/sideeffect-*",bumi_dir+"/output/number-of-authored/sideeffect")
