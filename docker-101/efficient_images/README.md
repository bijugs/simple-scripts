# Efficient Images

## Cheat Sheet

* `man docker-run`
* `man tac`
* `docker-image-history --format="{{.CreatedBy}} --no-trunc"`
* [`.dockerignore` documentation](https://docs.docker.com/engine/reference/builder/#dockerignore-file)

## Pre-Lab Spring Cleaning

Just to make sure we're all cleared out from the last lab, go ahead and run
this command:

``` console
$ docker rm -f $(docker ps -aq)
$ docker rmi -f $(docker image ls -q)
```

## Bad Dockerfile, Bad!

We've provided a Dockerfile that exhibits all of the anti-patterns we discussed
before.  Take a look inside and spend some time understanding what's going on.

Go ahead and build this Dockerfile.  Observe all of the commands being run, and
note how long the build takes.

Now build it again using the exact same command.  Note that the build is close
to instantaneous, and that all of the steps were grabbed from the cache.

Now open the `app.py` file and change the message it prints.  Rebuild the image
and observe which layers are pulled from the local cache vs rebuilt from
scratch.  How long does the build take?

Now change _anything_ in the Dockerfile.  Seriously, just add a newline
somewhere.  Rebuild the image and observe the entire world being rebuilt.  Why?

Run the image again, but this time using an interactive bash shell.  Take a
look at the files in the `/app` directory.  Do you see any that shouldn't be
there?

## Make it Better

This step is easy - Fix the Dockerfile!

The first think you'll want to do is to tell `docker build` that it should
ignore the Dockerfile - otherwise every change you make will trigger a new
build because of the `COPY . .` command.   While you're in there, tell `docker
build` not to copy in the sensitive `luggage_combination.txt` file as well. Now
that we have a `.dockerignore` file, we should probably add that to itself -
inception!

Now fix the ordering.

> **Remember:**  We want to move the things that change more frequently to the
> bottom, so those changes don't invalidate the cache for later instructions.
> However, we _do_ need to make sure we maintain the correct order for some
> instructions - for example, you'll want to make sure you `WORKDIR /app`
> before you `COPY . .`

You can test your changes by building and running the image (probably easiest
if you tag the image while building).  If you see the message from `app.py`
printed to the screen, you probably haven't screwed it up.

If you've achieved the optimal ordering, you'll be able to modify `app.py`
without triggering reinstallation of any Python packages.   You'll also be able
to add a python package (`flask`, for example), without triggering the
reinstallation of Emacs and Vim.

## Rooting Around

Now that we have a somewhat normal Dockerfile, let's take a look at the images
it produces.  Build a new image from this Dockerfile, name it `finally`, and
take a look at the layers using the `docker image history` command (see the
Cheat Sheet above for help).

Brownie points if you can figure out where all of these layers are coming from.

## Cleaning Up

Let's clean up our work by running:

``` console
$ docker rm -f $(docker ps -aq)
$ docker network prune -f
```

## Further Reading

Once again, the [Dockerfile Best
Practices](https://docs.docker.com/develop/develop-images/dockerfile_best-practices/)
guide has some useful information for reducing your image size and encouraging
layer reuse.

An explanation of why [you may want to be recreating your images without
caching](https://pythonspeed.com/articles/docker-cache-insecure-images/) every
so often.

[Dive](https://github.com/wagoodman/dive) - An interesting tool that lets you
explore Docker images in a terminal-file-browser-like interface.

A beautiful [rant on the use of Tar as the underlying image
format](https://www.cyphar.com/blog/post/20190121-ociv2-images-i-tar) - lots of
interesting bits on the history of Tar and why it's not the venerable and
stable file format we might have thought it to be.

A warning about [multi-stage images and caching in CI
environments](https://pythonspeed.com/articles/faster-multi-stage-builds/).
