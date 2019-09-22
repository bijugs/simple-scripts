# Networking

## Cheat Sheet

* `man docker-run` -- in particular, the `-d`, `-p`, `-P` and `--net` flags.
* `man docker-inspect`
* `man docker-network-inspect`
* `man docker-network-create`

## Pre-Lab Spring Cleaning

Just to make sure we're all cleared out from the last lab, go ahead and run
this command:

``` console
$ docker rm -f $(docker ps -aq)
```

## Nano-service

First, let's create a service to experiment with.  We've provided a simple
Dockerfile and `app.py` file that serves requests on port `8000`.  Read through
both of those to understand them -- in particular, note that we're using the
`EXPOSE` instruction in the Dockerfile.  Go ahead and build this image, tagging
it `app`.

## Ports

### Explicit

Now that we have an image, let's use it to explore the various ways of exposing
ports.  First, let's run the image in the background, with the `-p` flag to
forward local port `80` to port `8000` on the container.

Use the `http` command to make a request against `localhost:80` and verify that
the request was routed to your containerized application.

Kill the container to clear the way for the next section.

### Random

Let's re-run the image, again using the `-p` flag, but this time only
specifying the container-side target port.  Use `docker ps` to determine what
local port the Docker daemon chose, and use `http` to make a request against
it.

Kill the container to clear the way for the next section.

### Exposed

Finally, let's make use of the fact that our image included the `EXPOSE`
instruction to mark which port it listens on.  Run the image one last time, but
with the `-P` (capital "p") flag.

This time, use `docker inspect` to determine what local port the Docker daemon
chose, and use `http` to make a request against it.

Kill the container to clear the way for the next section.

## Default Bridge

Let's explore the default bridge network that Docker provides.

First, let's list our networks using `docker network ls` - note that we have
three available: "none", "bridge", and "host".

Launch your `app` image in the background without exposing any ports. Use
`docker network inspect` to look at the details of the bridge network -- note
that our container is listed in the output.

We'd like to connect to `app` from another container, but the default network
doesn't provide DNS services.  It's up to us to figure out the IP address. Find
the IP address using `docker inspect` (we'll call this `$IP` below).  This IP
was also in the `docker network inspect` output, but it's good to know both
ways of getting it.

Next, launch an interactive shell using `superorbital/bash`.

Since we didn't specify `--net` for either of these containers, they are both
on the default bridge network, meaning they can reach each other at an IP
level.

In the `superorbital/bash` shell, run `curl $IP:8000`, and bask in our
connectedness.

Exit the shell and `docker rm -f` both of our containers to clear the way for
the next section.

## User-Created Bridges

User-created bridges offer a lot of useful features over the default `docker0`
bridge -- the most convenient is the automatic DNS entries.  Let's explore!

First, let's create a new network named `mynet` and verify with `docker network
ls`.

Next, launch the `app` image in the background, inside this network, naming the
container `myapp`.

Launch an interactive `superorbital/bash` shell in `mynet` and run `host myapp`.
Next use `http` in that shell to make a requests against `myapp:8000`.  Bask in
the glory of DNS!  While you're in the shell, take a look at the
`/etc/resolv.conf` and `/etc/hosts` files.

Bridge networks are isolated from each other.  If you create a new network
named `anothernet`, and launch an interactive shell inside it, you'll see that
you cannot resolve `myapp`, nor can you connect to the `myapp` container via IP
address.

Do _not_ kill the `myapp` container just yet.

## Under the Hood

With `myapp` running in the `mynet` network, let's take a look around.

`ip addr show` is a command that shows all of the network interfaces attached
to a host. The Docker bridge network driver creates new networks that are
connected via bridges and virtual interfaces. If you run `ip addr show` on the
host (not inside the container), you'll see three devices:

* The `docker0` bridge for the default network
* A `br-...` bridge
* And a `veth...` interface

To trace the thread through, we can determine that the `br-...` bridge is the
bridge for our `mynet` network by looking at the output of `docker network
inspect` and matching the Gateway IP address with the output above.

Once we know that, we can determine that the `veth...` device is bridged to it
by looking at the output of `ip addr show veth...`, or via the easier to use
`brctl show` command.

To clean up for the next section, delete both your network and any running
containers.

## Host Networking

Host networks are both the simplest and the most dangerous available.

Redeploy our `app` image, again in the background, but this time specifying
`--net=host`.

With the host network, there's no need for the `-p` or `-P` flags.  The
container and the local host share the same IP address, network interface,
routing rules, etc.  From your workstation, you can `http localhost:8000` and
get a response from the running container.

This is dangerous, because there's nothing stopping that container (which was
launched by a regular user) from exposing a privileged port (such as `80`,
`443`, `22` or `53`).

> Never run unknown containers in the host network, and never give untrusted
> users the ability to run `docker` commands.

## Cleaning Up

Let's clean up our work by running:

``` console
$ docker rm -f $(docker ps -aq)
$ docker network prune -f
```

## Further Reading

[Docker Networking Overview](https://docs.docker.com/network/)

[Bridge Networking
Documentation](https://docs.docker.com/network/bridge/) --
note that this document implies that containers on the default
network cannot talk to each other by default.  Don't be
fooled!  It's [a documentation
bug](https://github.com/docker/docker.github.io/issues/8973).

[Host Networking Documentation](https://docs.docker.com/network/host/)

[Way more information on Docker networking than you'll ever need](https://success.docker.com/article/networking)

[Deep dive into container communication](https://docs.docker.com/v17.09/engine/userguide/networking/default_network/container-communication/)
