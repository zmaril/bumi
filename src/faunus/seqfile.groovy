bumi_dir = System.getenv().bumi_curr

g = FaunusFactory.open(bumi_dir+"/resources/cassandra.properties")
g.setOutputLocation(bumi_dir+"/output/seqfile")
g._().submit()