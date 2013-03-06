bumi_dir = System.getenv().BUMI_CURR

g = FaunusFactory.open(bumi_dir+"/resources/cassandra.properties")
g.setOutputLocation(bumi_dir+"/output/seqfile")
g._().submit()