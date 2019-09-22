# Under the Hood

In this lab, we're going to explore cgroups and namespaces, and then we'll
practice debugging single-binary containers.

## Cheat Sheet

* `man ps`
* `man stress`
* `man unshare`
* `man nsenter`
* `man docker-run`

## Starting Fresh

Let's clean up our work from our previous lab by running:

``` console
$ docker rm -f $(docker ps -aq)
```

## Exploring cgroups

Let's experience the stifling boundaries cgroups can impose on a lowly
process.

For this and the next section, you'll need two terminal windows open.

In the first window, find your shell's process ID by running `echo $BASHPID`.
We'll use that in a later step.

In the second window run `sudo bash` in order to become the root user.
Managing cgroups is a privileged operation, and can only be done by root.

Now that we've put on our God hat, create a new memory cgroup named `cozy`.
Creating cgroups is as simple as creating a directory, so to create `cozy`,
you'd just run `mkdir /sys/fs/cgroup/memory/cozy`.

This is a special "virtual" filesystem that acts as an API into the Kernel.
These virtual filesystems often have strange properties.  For example, list
the files in the `cozy` directory you just created - note that these were
automatically populated for you.

> **Note** If you make a mistake, you cannot delete this directory with our
> normal `rm -rf` command.  Instead, you have to use `rmdir`.

Next, let's limit how much memory processes in this cgroup can use.  We can
write a value to the `memory.limit_in_bytes` file to set this like such:

``` console
$ cd /sys/fs/cgroup/memory/cozy/
$ echo 100000000 > memory.limit_in_bytes
```

Finally, let's add our shell from the first window to this cgroup.  Grab the
process ID you gathered before (for example, 12345) and run:

``` console
$ cd /sys/fs/cgroup/memory/cozy/
$ echo 12345 > tasks
```

Back in your first window, you'll see that you can run most commands just fine
(`ls`, `ps`, `cat`, etc), but if you run something that uses more than your
allotted memory...

``` console
$ stress -m 1 --vm-bytes 100000000
```

What happened?  Run `dmesg` to see what the kernel has to say.

## Exploring Namespaces

Next, we'll explore namespace isolation as done by hand.

Let's get out of our cgroup hell by exiting our first window and connecting
again.

In this first window, run `ps aux` to list all of the processes on your
workstation.

Now, run `sudo unshare --pid --mount-proc --fork bash` to enter a new shell
that has a completely isolated view of the process table.  Inside this new
shell run `sleep 1000 &` (just to have a process to view) and then run `ps
aux` again. What do you see?  What's the PID of your `sleep` process?

In your second terminal window, run:

``` console
$ ps -eo pid,ppid,command | grep sleep
```

Observe that the process ID is different inside the namespace than it is on
the host.  Observe the `ppid` value - this is the parent process ID, which is
the PID of your first window's bash shell, as seen from outside the namespace.

Finally, in the second window (the one _not_ inside a namespace), run:

``` console
$ sudo nsenter --pid --root -t 12345 bash
```

(Replace 1234 with the PID of your first window's bash shell from the step
before.)  You should now have two shells that are sharing the same process
namespace.  Run `ps aux` in both and observe that they're identical.

## Debugging Slim Containers

Finally, we're going to practice attaching utility containers to slender
application containers that don't include our debugging tools.

Let's exit both terminal sessions, and reconnect to your workstation.  You'll
only need one terminal for this section.

We've provided a Dockerfile that builds a single-binary C++ application
(`app.cpp`) from `scratch` (you may recognize it from a previous chapter).  Go
ahead and build this image, tagging it `slender`, and run it like so:

``` console
$ docker run -d -p 8000:8000 --name=app slender
```

Make some requests against the container, either using `http :8000`, or from a
browser on your laptop.

Let's say we wanted to watch packets going into and out of our application
from inside the container using the venerable [tcpdump
tool](https://www.tcpdump.org/).  Go ahead and run `docker exec app bash` -
what do you see?

The only binary in our image is the `app` itself.  We don't have `bash`, let
alone `tcpdump`, and your DevSecOps guru says installing either on a
production image is a Very Bad Idea™️.

Instead, we can build a quick image that executes `tcpdump`, and run it in a
new container connected to our `app` container's namespaces.

We could create a new Dockerfile for the `tcpdump` image, but this is a great
opportunity to use `docker build`'s `-` flag. Run the following:

``` bash
docker build -t tcpdump - <<EOD
FROM ubuntu:18.04
RUN apt-get -y update && apt-get -y install tcpdump
ENTRYPOINT tcpdump
EOD
```

> **Pro Tip:** `docker build -` takes the Dockerfile from standard input,
> instead of looking for it in a file.  Also, the `<<EOD` syntax is a Bash
> HEREDOC.  It pipes whatever is between the first and last `EOD` markers into
> the standard input of the command.

Great!  We now have a `tcpdump` image.  Let's run it, but connect it to the
network namespace for our `app` container:

``` console
$ docker run -it --rm --network=container:app tcpdump
```

Make some more requests to the `app` container and observe the network packets
coming and going!  You're a debugging master!

## Cleaning Up

Let's clean up our work by running:

``` console
$ docker rm -f $(docker ps -aq)
```

## Further Reading

Here's a great article [detailing the debugging technique we just
used](https://medium.com/@rothgar/how-to-debug-a-running-docker-container-from-a-separate-container-983f11740dc6).

I'm probably unique in this regard, but I love trolling through Linux man
pages.  Here are some that discuss the deep details of cgroups and namespaces:

* `man cgroups`
* `man unshare`
* `man 2 unshare`
* `man namespaces`
* `man setns`
* `man pid_namespaces`
* `man user_namespaces`
* `man namespaces`
* `man nsenter`

This is a bit old and outdated, but still somewhat useful: [documentation on
the v1 memory cgroup
API](https://www.kernel.org/doc/Documentation/cgroup-v1/memory.txt)

And a newer but less complete [documentation on the current v2 cgroups
API](https://www.kernel.org/doc/Documentation/cgroup-v2.txt)
