# bumi 

Bumi takes any project stored in git and loads it into a
[Titan graph database](http://thinkaurelius.github.com/titan/). 

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
this up as I go.

### Getting started 

Grab a largish instance on AWS and install git, R, leiningen, etc.
until everything works. 

Clone your project locally. 

Clone bumi locally. 

Set `BUMI_GIT_DIR` in bash to the git project you want to load. 

Run `lein run load` to load up the database. 

Run `lein run analysis` to run whatever analysis I've been messing around with as of late.  

Yell at me if it doesn't work. 

### Warnings

Bumi doesn't actually upload any of the code into Titan. It was
affecting performance and storage and I have no intention of using the
actual content of the messages or commits anytime soon.

This has *only* been tested with the Linux kernel. Don't think of it
as being untested though. Rather, consider yourself a software
explorer. You'll be discovering uncharted land, chatting with the
natives, and abusing computers all at the same time. 

## License

Copyright © 2013 [@ZackMaril](http://www.twitter.com/ZackMaril)

Distributed under the MIT license. 