# Containerizing Applications

## Cheat Sheet

* `man docker-container-export`
* `apt-get update && apt-get install -y --no-install-recommends`
* `docker run $(docker build -q .)`
* `docker ps --latest`
* [Pipenv Documentation](https://docs.pipenv.org/en/latest/)

## Python

In this section, we're going to containerize a Python application from scratch.

All of the work for this section should be done in the `python` subdirectory.
Go ahead and `cd` into that directory now.

Here you'll find some files:

* an empty Dockerfile,
* an `app.py` that imports Flask,
* a `Pipenv` file that references the Flask package,
* an `templates/index.html` that references `styles.css`, which needs to be
  compiled from...
* a `styles.scss` file, which can be compiled with the 3rd party `sassc` tool
  (that's not a typo - the tool is called `sassc`, but the format is `scss`).

Take a look inside each of these files.  We'll be tying all of these pieces
together.

### Generating Our requirements.txt

While we won't be using `pipenv` to run our application, it's a great tool for
managing versions while developing, and for locking those versions down in our
`requirements.txt` file in preparation for deployment.

Go ahead and run `pipenv lock -r > requirements.txt` to generate the standard
`requirements.txt` file.  Take a look at that file and note all of the packages
and their versions.  By using this file, we're pinning the versions of each
package, and avoiding any surprises in production.

### Create the Dockerfile

Now it's time to create our Dockerfile.  Remember the general order:

1. System dependencies
2. Application Libraries
3. Application Code

With that in mind, let's get started:

* Inherit from `python:3.7.3`
* Set the working directory to `/myapp`
* Copy in the `requirements.txt` file
* Install the packages via `pip install -r requirements.txt`
* Copy in the rest of the files
* Configure containers created from this image to run `app.py` via `python3`

### Run It

Build the image, and run it with the `-p 80:8000` flag.  Open your browser to
your workstation hostname and you should see a hello message!

Hoooooowever...  you'll also see an error message.  Looks like we forgot to
compile the `styles.scss` file 🤷

### Compile the SCSS

In your Dockerfile, use `apt-get` to install the `sassc` command.  In keeping
with the ordering above, you'll want to insert this `RUN` instruction somewhere
before the `COPY requirements.txt .`

Next, in our Dockerfile, we'll want to run `sassc styles.scss >
static/styles.css`. Obviously, we can't do that until we've copied that file
in.

Once you've added the three instructions above, rebuild the image, rerun the
container, refresh your browser, and verify that you no longer see the error
message.

### Clean It Up

One last thing.  Let's use the `docker export` command to take a look at the
contents of our image.  Export works on containers (not images) so you'll have
to figure out the name of the last container you ran from the previous section.

```
$ docker export CONTAINER_NAME | tar -tf - | grep myapp
```

What files do you see?  See any that we don't need in the image?  If so, add
them to your `.dockerignore` file and rebuild/rerun/retest.

## C++

All of the work for this section should be done in the `cpp` subdirectory.
Go ahead and `cd` into that directory now.

We've provided a Dockerfile and an `app.cpp` file.  The Dockerfile is already
configured to build and run our application.  Take a look at both files to
understand what they're doing.  Once you're happy, build the image, run it
(using `-p 80:8000`), and check it out in your browser.

> **Why are we using Alpine?**  Statically linking the GNU libc library that
> ships with most Linux is "difficult" (see Further Reading).  The end goal is
> to get our application running in a "scratch" container, without any dynamic
> libraries (including libc) so we need to use an alternative libc
> implementation named [Musl](https://www.musl-libc.org/).
>
> While we [don't recommend Alpine for a runtime
> image](https://kubedex.com/base-images/), the fact that it's build around
> Musl makes it a great builder image.

Now grab an interactive shell using this new image.  Since this is Alpine, you'll need to use `sh` instead of `bash`:

``` console
$ docker run -it $(docker build -q .) sh
```

Let's understand what's installed.

Clearly, we have `g++` installed (as we use it to compile our application).
But if you look at the output of the first `docker build`, you might have
noticed that we have Python installed as well! Verify that you can see those
applications in the container.  That's a lot of baggage to be carrying around,
and every extra utility could be used to escalate privileges by a hacker.

Back in your workstation, figure out the size of the image via `docker
container ls` - it should be around 200MB, which is roughly 180MB larger than
our compiled application.

Let's modify our Dockerfile to use static compilation and multi-stage builds to
reduce the size and increase the security of our image.

### The Ol' Two-Step

To convert our Dockerfile to a multi-stage build, do the following:

1. Add `as build` to the end of the `FROM` line.
2. Remove our `CMD`
3. Add the following to the end of the file:

``` dockerfile
FROM scratch
WORKDIR /
COPY --from=build /app/app .
CMD ["/app"]
```

Now build and run the image.  You should get a rather cryptic "no such file" error. Let's explore.

We can't grab a shell in our "scratch" image (since it doesn't have one), but
we can build just the `build` stage and poke around in there.  Run the
following commands:

``` console
$ docker build . --target=build -t build
$ docker run build ldd /app/app
```

Look how many libraries `app` expects to be able to load!  No wonder it won't run!

### Gettin' Static

Let's statically compile our binary so these libraries are embedded inside.
There are two changes you need to make to the Dockerfile to do this:

1. Replace `apk add ... libevent` with `apk add ... libevent-static`
2. Add `-static` to the end of our `RUN g++` line.

Now build and run our application to make sure it works.  Finally, take a look
at the image size and bask in your efficiency!

## Cleaning Up

Let's clean up our work by running:

``` console
$ docker rm -f $(docker ps -aq)
$ docker rmi -f $(docker image ls -q)
```

## Further Reading

The [12 Factor](https://12factor.net) application manifesto - the bible for
all cloud-native developers.

A [very complicated
Dockerfile](https://anonoz.github.io/tech/2018/05/01/rails-dockerfile.html)
for containerizing a Ruby on Rails application, without including the NodeJS
bits that are required for Sprockets, and that uses
[wkhtmltpdf](https://wkhtmltopdf.org/). This Dockerfile makes heavy use of
multi-stage builds.

[The glibc FAQ](https://sourceware.org/glibc/wiki/FAQ#Even_statically_linked_programs_need_some_shared_libraries_which_is_not_acceptable_for_me.__What_can_I_do.3F)
that explains why `g++ -static` doesn't actually produce true static binaries.

A great article on [the reasons to use static binaries in Docker images](https://www.ianlewis.org/en/creating-smaller-docker-images-static-binaries).

Documentation on [multi-stage
builds](https://docs.docker.com/develop/develop-images/multistage-build/).

And finally, someone went through the trouble of figuring out [how to use multi-stage builds with a Python application](https://pythonspeed.com/articles/multi-stage-docker-python/).
