# Understanding Containers

A quick lab to get our feet wet and explore this brave new world of
containers.

## Cheat Sheet

We'll often include a "Cheat Sheet" section in each lab, which we'll fill with
helpful (if sometimes cryptic) clues and reminders.

If you want online help throughout the next two days, these links might be
useful:

* [Docker Documentation](https://docs.docker.com/)
* [Docker CLI Help](https://docs.docker.com/engine/reference/commandline/cli/)

> **Pro Tip:** If you're reading this on your terminal, you may be able to open
> URLs by holding `Command` or `Alt` and clicking on the link.

You can also find general help by using the `--help` flag on a command:

``` console
$ docker image history --help
```

...or by reading the "man pages".  To get the man page for the `docker image
history` subcommand, you'd replace the spaces with dashes and run:

``` console
$ man docker-image-history
```

> **Pro Tip:** Some of the more common Docker commands are
> actually aliases.  For example, if you `man docker-push`,
> it tells you that it's an alias for `docker image push`.
> You'll get more information if you run `man
> docker-image-push`.

You can also run `man man` in your workstation, if you're unfamiliar with
UNIX man pages.

Check out these commands to help out with this particular lab:

* `man docker-run`
* `man ps`
* `man ifconfig`
* `man id`

You should always feel free to grab your instructor for help, but the learning
will stick better if you first try to figure the problem out yourself.

## Running our First Container

Let's run our first container using `docker run`.  Execute the entire command below:

``` console
$ docker run --rm -it bash
```

> **Note:** in this, and all future commands, the `$` indicates the prompt -
> you don't type it.  Also, we'll use `$` to indicate a prompt on your
> workstation, and `#` to indicate a prompt inside a container.


From now on, we'll assume you can figure out what all of the flags mean by
looking at the online documentation, through the `--help` flags, or by reading
the `man` page.  But just this once, we'll help you out:

* `docker run ... bash` downloads the `bash` image from Docker Hub and runs it.
  This is an official image maintained by the Docker team.  How do I know?
  Because it doesn't have an organization name prefix the way
  `gitlab/gitlab-ce` does.  Docker runs Docker Hub, and enjoys some kingly
  privileges.
* `--rm` (short for remove) tells Docker to delete
  the container when we're through.
* `-i` (or `--interactive`) allows the command to read STDIN from our terminal
  through docker.
* `-t` (or `--tty`) allocates a "pseudo-TTY", effectively convincing the
  command that it has a keyboard and a screen attached.

You'll often see `--rm -it` combined when running interactive commands like a
shell.

Back to your terminal, and CONGRATULATIONS!  You're now running a bash shell
inside a container!  Are you excited?!  I know I am!  Let's take a look around.

## Looking Around

You should be at a prompt that says `bash-5.0#`.  From here you can run any
command you wish inside your container.  Keep in mind that this container
isn't running Ubuntu, but a much smaller distribution named
[Alpine](https://alpinelinux.org/), so the commands available are usually
stripped down versions.

> **Note:** When we say "running," that's technically a tad misleading.  To
> be more precise, the container is mounted against an Alpine root
> filesystem. This means that any command you run will be from the Alpine
> distribution, and that the container will feel like an Alpine VM for all
> normal purposes. However, the kernel is still the same one running your
> workstation, `N.classroom.superorbit.al`.

### Users and Permissions

Let's get our bearings.  First, let's figure out who we are.  Run the `id`
command inside the container and observe what it tells us.

Next, let's look at the filesystem.  Go ahead and `cat /etc/shadow` (a
protected file that usually contains sensitive information).  Now let's `touch
/the_sky`. Clearly, we have real `root` privileges inside our container.

Exit our shell so you're back at your workstation.  If you run `ls /the_sky`,
what do you see?  This proves to us that our container filesystem is completely
isolated from that of our host.

Exit the container (either via `exit` or by typing `Ctrl+D`) and start the
shell again inside a new container (`docker run -it --rm bash`).  Is the
`/the_sky` file still there?

> **Remember:** our containers are completely ephemeral they come and go like
> dust in the wind.

### Networking

While in the container, run `ifconfig eth0`.  Observe the IP address and
network mask. Back on the host, run `ifconfig docker0`.  You'll see that this
interface is on the same network as the container.  We'll explore the
networking side of Docker in a later chapter.

### Processes

Run `ps` in the container and observe that the only processes you see are
`bash` and the `ps` command you just ran.  There's no `sshd`, `systemd`,
`cron`, `syslog`, etc.  Just you and `bash`.  Also, note that `bash` has PID 1.
Some of you might raise an eyebrow at that.  We'll talk about the consequences
later on.

The important bit is that the container isolates you from the rest of the
processes on the host.

Now, inside the container, run `sleep 1000 &`.  This runs the `sleep` command
(which does nothing for 1,000 seconds), and puts it in the background via `&`.
Next, run `ps` inside your container and note the values.  Finally, open
another ssh session to your workstation (not inside the docker container) and
run:

``` console
$ ps -eo pid,user,command | grep sleep
```

This is the same process, both in and outside of the container, however,
the PIDs inside and outside of the container are different. These mappings
are part of the magic of containers!

### Layers Upon Layers

You probably noticed that the `docker run` command didn't just start up the
container.  It first had to "pull" the image, which means it had to search
Docker Hub for the image named `bash`, and download it locally.

```
Unable to find image 'bash' locally
latest: Pulling from bash
c729a5138a60: Waiting
ab3d5dc0b96d: Downloading [====>                                              ]  155.5kB/2.017MB
6f17aebaff06: Pull complete
Digest: sha256:24832df4a1c2b4610e5175a498103e24f7c45b0990cee7c55f74b290336b460d
Status: Downloaded newer image for bash:latest
```

Images are comprised of "layers", in order to make this distribution more
efficient. Docker caches the image locally, which is why you didn't see it
downloading the layers when we ran `docker run` a second time.

### Breaking Free

Just to illustrate the fact that Docker ≤ Security, exit your container and
restart it using this command:

``` console
$ docker run -it --rm -v /:/host bash
```

Now, while in the container, create a new file under `/host`.  Be careful not
to modify any other files in `/host` - if you do, we'll have to recreate your
workstation (!!!)

Exit the container, and observe that the new file exists in your workstation,
and note the owner.

Don't worry -- We'll explain what we just did with the `-v` flag in the storage
chapter later on.

## Further Reading

We'll be including interesting articles or documentation pages here, in case
you finish the lab before others, or if you just want some take home reading.

[What even is a container?](https://jvns.ca/blog/2016/10/10/what-even-is-a-container/)
