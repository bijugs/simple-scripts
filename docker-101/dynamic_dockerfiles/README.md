# Dynamic Dockerfiles


## Cheat Sheet

* [The Dockerfile `ARG` instruction](https://docs.docker.com/engine/reference/builder/#arg)
* [The Dockerfile `USER` instruction](https://docs.docker.com/engine/reference/builder/#user)
* `man useradd` & `man groupadd`

## Starting Fresh

Let's clean up our work from our previous lab by running:

``` console
$ docker rm -f $(docker ps -aq)
```

## Naive "grab" image

We'd like to make a Docker image that downloads files by URL to our current
directory -- just packaging up the `curl` command.  We're going to use the
`ENTRYPOINT` pattern to make this image more naturally callable on the command
line.

We've provided a Dockerfile for you.  Go ahead and take a look.

``` dockerfile
FROM ubuntu:latest

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
      ca-certificates \
      curl

# Intended to be run with
# --mount type=bind,dst=/host,src=$PWD
WORKDIR /host

ENTRYPOINT ["curl", "-sSLO"]
CMD ["https://httpbin.org/json"]
```

Run it like such:

``` console
$ docker build -t grab .

$ docker run \
$   --mount type=bind,src=$PWD,dst=/host\
$   grab https://httpbin.org/xml
```

You should now have a file in your local directory named `xml` (that hopefully
contains some XML).  Run `ls -l xml` - do you notice anything strange about
this file?

Your Docker image runs as root (UID 0) by default, and since the file system
is mounted in both the workstation and container, files created by the
container are _also_ created and owned by UID 0.

This isn't good.  Remove this file so we can try again.

## Running as a Our Host UID

To fix this, we can create a user with our local student UID (from the
workstation), and use the `USER` instruction to run as this new user.

Run `id` locally to double check that your user ID is `1002`, and that your
student group id is `1004`.  Now, add the following stanza under the `apt-get
install` instruction in the Dockerfile:


``` dockerfile
RUN groupadd --gid=1004 mygroup
RUN useradd \
      --home-dir=/home/myuser \
      --no-log-init \
      --create-home \
      --shell=/bin/bash \
      --gid=1004 \
      --uid=1002 \
      myuser

USER myuser:mygroup
```

> What would have happened if you'd placed this **before** the `apt-get
> install`?

Rebuild and re-run the image using the commands above.  You should see that
the new `xml` file is owned by ourselves!

> If instead you get `curl: (23) Failed writing body (0 != 522)`, then you
> probably forgot to remove the root-owned xml file beforehand.

You can also test this by running:

``` console
$ docker run -it --rm --entrypoint=/bin/bash grab
```

And viewing the output of `id` inside the container.  The UID and GID should
match what you saw on your workstation.

But, wait...  We've just hard-coded our local UID and GID into the Dockerfile.
We'd like to be able to share this Dockerfile with our coworkers so they too
can use `curl` in the most convoluted way imaginable.  How can we make these
magic numbers configurable?

## Enter `ARG`!

All we need to do is parameterize 1002 and 1004 as build arguments!  Declare
them just below the `FROM` line like such:

``` dockerfile
FROM ubuntu:latest

ARG UID
ARG GID
```

Now, replace all occurrences of `1002` with `$UID` and all `1004` with `$GID`.

Finally, rebuild your image with the `--build-arg` flags and run it like such:

``` console
$ docker build -t grab \
$   --build-arg UID=$(id -u) \
$   --build-arg GID=$(id -g) \
$   .

$ docker run \
$   --mount type=bind,src=$PWD,dst=/host\
$   grab https://httpbin.org/json
```

> The `$(id -u)` construct just returns 1002.  If you're uncomfortable with
> such Bash magic, you can also simply type it out as `--build-arg UID=1002`.

You should now have a file named `json` in your directory, properly owned by
you.

## Caching

Let's now pretend to be a coworker who's user has UID 1234.  Rebuild the image
with that value, paying attention to the build output:

``` console
$ docker build -t grab \
$   --build-arg UID=1234 \
$   --build-arg GID=1234 \
$   .
```

How many layers did `docker build` re-build instead of grabbing from the
cache?  Re-order our `ARG` and `RUN` instructions in the Dockerfile to
optimize build times.

## Parameterize the Tag

Finally, our Dockerfile is using `FROM ubuntu:latest`.  Not only is `:latest`
a bad practice, but maybe it'd be nice to be able to specify which version of
Ubuntu to use from the command line (say, for CI/CD purposes).

`ARG` is the only command that can come before the first `FROM` statement -
just for this use case.  Go ahead and parameterize the image tag, defaulting
to `latest` and rebuild using the venerable `16.04`.

## Cleaning Up

Let's clean up our work by running:

``` console
$ docker rm --force $(docker ps -aq)
```

## Further Reading

[Why you want to pass `--no-log-init` to your `useradd` command in a
Dockerfile.](https://github.com/moby/moby/issues/5419)

One of many [articles describing the solution we've just
implemented](https://jtreminio.com/blog/running-docker-containers-as-current-host-user/).

Another solution that [dynamically creates the user at
_runtime_](https://denibertovic.com/posts/handling-permissions-with-docker-volumes/)
via a custom `ENTRYPOINT`.  Better in that there's only one image to be built.
Worse in that the image still runs as `root` (and takes a tad longer to start
up).
