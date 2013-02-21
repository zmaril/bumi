# bumi 

Bumi takes any project stored in git and loads it into a
[Titan graph database](http://thinkaurelius.github.com/titan/). Then
you point [Faunus](http://thinkaurelius.github.com/faunus/) at the
local database and you ask questions. 

That's about it. [Wahooo](https://www.youtube.com/watch?v=DwDefPNiAvg)!

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
this up as I go. 

### Getting started 

Grab a largish instance on AWS and install things until everything
works. 

Clone your project locally. 

Clone bumi locally. 

Set `BUMI_GIT_DIR` in bash to the git project you want to load. 

Set `BUMI_STORAGE_DIR` in bash to the directory you want the
Titan database to be stored in. 

Set `BUMI_DEBUG` to `TRUE` to see a bunch of scary warnings about
what's going on. 

Run `lein run uploader` to upload the repo. 

Wait. 

Point Faunus at the local repo and learn. 

### Warnings

Watch out for binaries and big blobs.

I've turned off parallelization because it messes with the locks, but
the Linux kernel loads within a day or two. I doubt you will find a
project with more history than the Linux kernel. (I've had success
using a Cassandra back end and pmap, but I decided to use BerkelyDB as
the back end because it is only one machine). 

This has *only* been tested with the Linux kernel. Don't think of it
as being untested though. Rather, consider yourself a software
explorer. You'll be discovering uncharted land, chatting with the
natives, and abusing computers all at the same time. 

## License

Copyright © 2013 Zack Maril

Distributed under the MIT license. 

It would be nice if you sent me a tweet,
[@ZackMaril](http://www.twitter.com/ZackMaril). In addition,
attribution would be nice if this were used for research. 
