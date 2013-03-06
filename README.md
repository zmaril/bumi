# bumi 

Bumi takes any project stored in git and loads it into a
[Titan graph database](http://thinkaurelius.github.com/titan/). A
growing variety of analysis tools are provided via Faunus and R
scripts.

[Wahooo](https://www.youtube.com/watch?v=DwDefPNiAvg)!

## Motivation 

I noticed that git projects form social networks. In particular the
Linux kernel has
[a style of commit message](https://github.com/torvalds/linux/commit/f9fd3488f6a3c2c5cc8613e4fd7fbbaa57f6bf8f)
that lends well to measuring and quantifying relationships between
developers and maintainers. Bumi loads the graph of commits into Titan
as well as any connections specified in the Linux style. I thought
others would be interested in analyzing their git repositories, so I
put this up on github. 

## Usage

So far Bumi has been used to load the Linux kernel into a Titan
database. It seems to work, but I'm tweaking the code and making all
this up as I go. I've included as part of this pipeline some analysis
tools I've built with Faunus and R. If your project doesn't have Linux
style commit messages, then some of the graphs produced will be quite
boring. 

### Getting started 

Grab a largish instance on AWS and install git, R, leiningen, etc.
until everything works. That means going in and installing `ggplot2`
for R as well. 

Clone your project locally. 

Clone bumi locally. 

Set `BUMI_GIT_DIR` in bash to the git project you want to load. 

Set `BUMI_FAUNUS_DIR` in bash to the location of faunus. 

Set `BUMI_DEBUG` to `TRUE` to see a bunch of scary warnings about
what's going on. 

Use the `bumi` bash file to do stuff. 

Yell at me if it doesn't work. 

### Warnings

Watch out for binaries and big blobs.

I've turned off parallelization because there are bugs, but
the Linux kernel loads within a day or so. I doubt you will find a
project with more history than the Linux kernel. (I've had success
using a Cassandra back end and pmap, but I decided to use BerkelyDB as
the back end because it is only one machine). TODO: check if it still
breaks after turning off diff's and message's. 

Bumi doesn't actually upload any of the commit messages or code into
Titan. It was affecting performance and storage and I have no
intention of using the actual content of the messages or commits
anytime soon. 

This has *only* been tested with the Linux kernel. Don't think of it
as being untested though. Rather, consider yourself a software
explorer. You'll be discovering uncharted land, chatting with the
natives, and abusing computers all at the same time. 

Currently, the R analysis tools will fail silently if something
doesn't work. So, watch out and check the .Rout files in the root dir
just in case. 

## License

Copyright © 2013 Zack Maril

Distributed under the MIT license. 

It would be nice if you sent me a tweet,
[@ZackMaril](http://www.twitter.com/ZackMaril). In addition,
attribution would be nice if this were used for research. 
