# Storage

This is a longer lab.  We're going to explore the root filesystem, Bind Mounts,
and Volume Mounts.

## Cheat Sheet

* `man docker-run`
* `man docker-container-diff`
* `man docker-container-ls`
* [The `VOLUME` instruction documentation](https://docs.docker.com/engine/reference/builder/#volume)

And the volume subcommands:

* `man docker-volume-create`
* `man docker-volume-ls`
* `man docker-volume-rm`
* `man docker-volume-prune`
* `man docker-volume-inspect`

## Spring Cleaning

Before we start this lab, let's make sure we're all reset from previous ones by
running:

``` console
$ docker rm -f $(docker ps -aq)
$ docker system prune -af --volumes
```

## Root Filesystem

Let's explore the root filesystem available to a container.

Run a simple interactive bash command in a container using the `ubuntu` image
(this time do _not_ use the `--rm` flag).  Inside the container:

1. Write a new file named `/etc/h4xored`,
2. remove the file named `/etc/legal`, and
3. append a line to the `/etc/passwd` file like such: `echo
   blackhat:x:1001:1001::/:/bin/bash >> /etc/passwd`

Exit your container and return to your workstation prompt.  Verify that the
container is stopped, but still available via `docker ps -a`.

We can get a quick list of changed files in a container's root filesystem
through the `docker container diff` command.  Run that command now and grok the
output.

Spawn another container using the same `docker run` command you ran earlier.
Observe that the changes you made have disappeared.  The writable layer of the
root filesystem is part of the container, and not part of the underlying image
- any changes are gone as soon as the container is removed, and changes are not
shared among other containers using the same image.

## Bind Mounts

Next, let's explore the most useful and simplest of the mounts - the bind mount.

Change into the `app` directory for this section of the lab.

We're going to use a bind mount to share our current working directory between
the host and the container in order to speed up our development cycle.

In this directory, you'll see a Python Flask application and a Dockerfile to
package it up.  Go ahead and build the image and run the application using the
following commands:

``` console
$ docker build -t myimg .
$ docker run -d --rm -p 80:8000 --name=myapp myimg
```

Open the application in your browser at http://N.classroom.superorbit.al (where
N is your number), and you should see "Hello World!"

Now, modify the `app.py` file to display a new message of your choosing.
Refresh your browser, and you'll still see the old greeting.  That's because we
haven't rebuilt and restarted the running container.  Rebuild the image,
`docker kill` the container, run it again, and refresh your browser to see the
change.

Obviously, the point of this is to show how onerous the development cycle
normally is when using Docker.  It's very easy to speed this up.  Note that
your Dockerfile is running the Flask application in the `/app` directory in
your container.  Run the application using the same command as above, but this
time add a `--mount` flag to create a bind mount between your current working
directory and `/app`.

> **Note** `src` cannot simply be set to `.`, so you'll need to type out the
> entire local director path or just set it to `$PWD`

Now, modify the local `app.py` file again, refresh your browser, and marvel at
the instantaneous feedback!

Now `docker rm -f` your running containers to clear the way for the next
section.

### Some issues with this technique

While this is a valuable tool in your belt, you do need to take care.

Since you're not actually rebuilding the image every time, the application code
will gradually become out of sync with the version stored in the image.  When
you're done with the local version, you'll need to remember to `docker build` a
final image to bake the changes in before pushing.  It's easy to forget this
step, and push the original image without your changes.

Similarly, the `/app` directory generally only contains the application code,
while libraries and system packages are installed via the Dockerfile globally
in the image.  This means you'll need to remember to recreate your image
whenever you need to change a library or package (including when you modify
`pipenv` or `Gemfile`).  If you forget this step, you'll be left wondering why
your application can't find the new library.

Finally, this trick only works with frameworks that support auto-reloading
(sometimes called hot-reloading or live-reloading).  Most web frameworks do,
but some require an extra step (such as setting `FLASK_DEBUG=1`).  We've
hard-coded this environment variable in our Dockerfile for simplicity, but if
this image were also used in production, we'd want to pass it in via the
`docker run --env` flag instead.

## Volume Mounts

Next, let's explore the relatively new Volume Mount features in Docker.

In order to allow volumes to outlive the containers that mount them, and in
order to allow for sharing, volumes are managed outside the container life
cycle.  Use the `docker volume` command to create a volume named `data`.

Now, run an interactive shell named `db` using the `ubuntu` image and mounting
the `data` volume as `/data`.

While inside the shell in the container, create some files in the `/data`
directory.

Now exit your shell, and observe that the container is stopped via `docker ps`.
Go ahead and kill it completely using `docker rm`.

Next, spin up a new interactive container, the same as before, but this time
name it `db2`.  Observe that you can still see the files you created in the
`/data` directory.

Now `docker rm -f` your running containers and `docker volume prune
-f` to give us a clean slate for the next section.

## The `VOLUME` Instruction

Finally, let's understand how the Dockerfile `VOLUME` instruction works, and
when it can be useful.

Change into the `db` directory for this section of the lab.

We've provided a `db.py` and a Dockerfile to package it up.  This application
acts as a poor-man's key/value store.  If you `POST /foo`, then it stores the
request body as the contents of the file `foo` in the container.  Similarly, if
you `GET /foo`, then it returns those contents.  It also lists all of the files
when you `GET /keys`.

Build this image and run it with the `-d -p 80:8000` flags.  In your local
terminal on the workstation, use the `http` command to read and write data like
such:

``` console
$ echo 'Some data.' | http http://localhost/one
$ echo 'Even more!' | http http://localhost/two
$ http http://localhost/keys
$ http http://localhost/one
$ http http://localhost/two
```

Now that we've seen how to exercise our database, let's back it with a volume.

The `VOLUME` instruction in your Dockerfile acts as a hint to `docker run` that
it should create and mount a volume at the specified path whenever you spin up
a container using this image.

This is commonly used by database images to provide an easy path for
exploration. If the image includes a `VOLUME` instructions, the end user can
just `docker run`, without having to use the `--mount` command and without
having to understand where the volume should be mounted inside the container.

First, remove the running container to clean our plate.

Now, replace the `mkdir` command in our Dockerfile with a `VOLUME` instruction
mounted at `/data`.  Build a new image, run the container again using the same
command you used previously, and exercise it with the `http` command.

If you look at the "Mounts" stanza in the `docker inspect` output, you'll see
that the container is backed by an anonymous volume that was automatically
created when we ran the image.  You can also see this volume in the `docker
volume ls` output.

It would be nice to have containers using our image start out with some sample
data. Docker will copy any data currently in our mount point to the new volume
(if the volume is empty) when it mounts it.  Let's use that to make some seed
data. Modify the Dockerfile to include these lines _before_ the `VOLUME`
instruction:

``` dockerfile
RUN mkdir /data
RUN echo "1" > /data/one
RUN echo "2" > /data/two
RUN echo "3" > /data/three
```

Recreate the image, delete and recreate the container, and run `http
http://localhost/keys` to see our seed data.

> **Note:**  Counterintuitively, if you'd put the `echo` commands _after_ the
> `VOLUME` instruction, the `one`, `two`, and `three` files would have been
> discarded.  I have no good explanation for why they chose to make this the
> user experience.  This is also true if the volume already contains data - the
> shadowed contents will _not_ be written into this volume upon mount.

The `VOLUME` instruction provides us with quick-and-easy persistence, but it
uses anonymous volumes with random names.  It's also sometimes surprising to
find that containers you've spun up are also creating volumes behind your back.

We can at least address the former issue by overriding the volume via the
command line.

Delete and recreate your container as per above, but this time add the `--mount
src=mydata,dst=/data` flag.  This will create a volume named `mydata`, and
mount it into place.  Because we've done this, `docker run` will _not_ create
an anonymous volume (it will effectively ignore the `VOLUME` instruction).

## Cleaning Up

Let's clean up our work by running:

``` console
$ docker rm -f $(docker ps -aq)
$ docker system prune -af --volumes
```
