# Using Docker

## Cheat Sheet

* `man docker run`
* `man docker build`
* `man docker exec`
* `man docker attach`
* `man docker logs`
* `man docker ps`
* `man docker kill`
* `man docker rm`
* `man docker cp`

The `--help` output and man pages are useful to quickly determine the command
use and options, but the online help pages have "Extended description" sections
that often have valuable information.

You can find the help pages for each subcommand in [the left-hand sidebar on
this page](https://docs.docker.com/engine/reference/run/) (or you can just
google "docker logs" -- the help pages are almost always the first link).

## Client, Meet Server

As we said before, Docker is a client/server architecture, meaning your
workstation has both the `docker` CLI (command line interface) as well as the
`dockerd` daemon, which is running right now.

You can observe this daemon process by running `ps -eF | grep dockerd`.  You
might also notice that the daemon references another process, Containerd.  The
Docker daemon provides the API, and offloads the actual container management to
Containerd.

## Turning Red

Let's spin up a container to play around with.  We have a fun little test image
called `superorbital/color` which provides a JSON API that simply returns the
configured color.  Use the `docker run` man page and the online documentation
to figure out how to:

1. Run the `superorbital/color` image
2. In a container named "red"
3. Daemonized (so it runs in background)
4. With the `COLOR` environment variable set to 'red'
5. Forwarding local port `8000` to port `80` on container

To verify that you got it right, you should see your container running when you
run `docker ps`.

## Making Requests

Let's test our port forwarding by making HTTP requests against the local port
`8000`.

You could use `curl` for this, but we prefer the more modern `http` command.
`http` will format the JSON output nicely, print the headers, and color code
the entire shebang.  Also, `http` has a nice shorthand for making local
requests.  Instead of typing `http http://localhost:8000`, you can just type
`http :8000` -- "http://localhost" is used by default.

> **Note**: `http` is actually called "Httpie". It's confusing. You can read
> more about it by running `http --help`, reading `man http`, or by
> [going here](https://httpie.org/).

So let's make a few requests against http://localhost:8000, which Docker will
politely proxy to our container on port `80`.  You should see that the color
has been correctly configured as "red".

After you've made a few requests, take a look at the logs for your container.
Just to exercise your knowledge, print out the logs with timestamps as well.

## Gaining Access

Let's jump into our container and take a look around.  Use the `docker exec`
command to run a `/bin/bash` shell inside our `red` container.  Remember that
you need to run bash as an interactive command with access to your TTY.

Curious about the source code?  Take a look around the `/app` directory.  What
HTTP endpoints does the `color` application respond to?

Note the `/.dockerenv` file.  It's always empty, but it's a useful file to know
about if you ever need to determine in a script whether or not you're running
inside a Docker container.

## Signaling Our Intentions

Exit out of the container, so you're back on the host.

Let's run another useful image we've provided to play around with signals.

1. Run the `superorbital/signal-catcher` image
2. Naming the container "catcher"
3. As a daemon (in the background)

Use the `docker kill` command to send a `HUP` signal to the catcher container.
Observe the logs to see it catch and report the signal.  Try other signals
(`USR1` or `USR2` for example).

Now send a `KILL` signal and watch the process actually die.  What does `docker
ps` say?

> **Note:**  We've used the term "daemon" a few times, now.  If you're not
> familiar, this basically means a process that runs in the background (though
> the implementation is much more complicated).  Daemon names generally end in
> the letter `d`, such as `dockerd`. You can [read more about them here](https://en.wikipedia.org/wiki/Daemon_%28computing%29).

## Necromancy

Find the stopped "catcher" container using `docker ps`.

Try to grab an interactive bash shell inside the "catcher" container.  Observe
our abject failure.

Even though the container is stopped, we can still get the logs.  Grab the last
3 lines of logs (without using the Linux `tail` command, ya cheater).

We can also use the `docker cp` command against stopped containers. Copy the
`/app/signal-catcher` file from the container to this directory.  Take a look
at the contents.  Bask in the beauty of Ruby!

## Taking Out The Trash

Stopped containers don't take up CPU or memory, but they do take up filesystem
space, and they just generally clutter things up.  Let's remove the container
using `docker rm`.

Verify that the stopped container is no longer there using `docker ps`. You
should only have the "red" container running at this point.

## Cleaning Up

We're all done with our lab, but there's one more chore.  To clear the room for
our next chapter, let's stop and remove the "red" container.  Do this with a
single command _only_ using the `docker rm` subcommand (`docker kill` is not
allowed).

Verify that you have no more containers either running or stopped.

## Further Reading

If you finished before the rest of the class, then congratulations on being a
stuck-up overachiever!  Feel free to read through some of these articles to
pass the time.

[An overview of the Docker components](http://alexander.holbreich.org/docker-components-explained/)

[Origin of the `/.dockerenv`
file](https://superuser.com/questions/1021834/what-are-dockerenv-and-dockerinit)
