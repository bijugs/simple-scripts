# The Dockerfile

A commonly shared misconception is that because docker makes running software
easy, it's probably easy to dockerize an application.  The truth is that
constructing a good Dockerfile is a bit of an art.  The best way to approach it
is to create the Dockerfile iteratively.

## Cheat Sheet

* `man docker build`
* `man docker run` (especially the `-p`, `-t`, `-i`, and `--rm` flags)
* `man dockerfile` is useful, but a tad outdated. For example, `MAINTAINER` is
  now deprecated.

But the best resource for constructing Dockerfiles is the [official Dockerfile
reference](https://docs.docker.com/engine/reference/builder/).  It outlines
all of the nitty-gritty of each Dockerfile instruction with lots of
explanatory text.  The instructions we went over were `FROM`, `ENV`,
`WORKDIR`, `RUN`, `CMD`, and `COPY`.

Finally, while linters can be opinionated (and sometimes outdated),
[hadolint](https://github.com/hadolint/hadolint) is still a great tool for
linting your Dockerfile.  It even runs the amazing
[shellcheck](https://www.shellcheck.net/) linter against any `RUN` lines.
We've installed it in your workstation.

## Simplest Dockerfile

Let's start with a very simple base.  Modify the Dockerfile in this directory.
Change it so that it inherits `FROM` Ubuntu version 18.04, and stop there.
That's the only instruction we need for a valid Dockerfile.

Now let's build and run our new image.  Build the image with `docker build`,
being sure to tag it with the name `app`.  Then, run bash inside the image via
`docker run -it app bash`

## Pythonize it

OK, we've got a base to work with.  Eventually we'll be deploying a Python
application, so let's modify our image to include the Python runtime.

Add a single `RUN` instruction that executes:

``` bash
apt-get update && apt-get install -y python3 python3-pip
```

Feel free to break the line up using backslashes (`\`)
right before you type a newline.  This just makes the file easier to read.  You
can also break the commands up into multiple `RUN` statements, but for
efficiency reasons that we'll go into later, it's not a great idea.

Now rebuild the image (remember to tag it `app`), and run it via `docker run
app python3 --version` to double check that it was installed correctly.

> If you get an error saying the `python3` executable can't be found, you may
> have forgotten to tag your most recent build with the name `app`.

## Copy in our application

Now we're ready to add our application code.  First, edit the `app.py` file, so
it looks like such:

``` python
if __name__ == '__main__':
    print('Hello from Python!')
```

Now, modify your Dockerfile.  First, we want to put our application in a
directory named `/app` (a fairly common convention).  Create this directory and
set it as the default using a single `WORKDIR` instruction.

Next, copy the `app.py` file from our local directory into `/app` in the
container.

Finally, configure our container to run `python3 app.py` by default.  Remember
to use the exec-form, not the shell-form.

Build the image, tagging it as `app`, and run it without any arguments.  If you
see "Hello from Python," then congratulations!

## Libraries

But this is clearly just a shim application.  Let's configure it to serve HTTP
traffic by using the Flask framework.

The first change is to our `app.py` file.  Make it into a simple Flask
application like such:

``` python
from flask import Flask
app = Flask(__name__)

@app.route('/')
def root():
    return 'Hello, World!'

if __name__ == '__main__':
    app.run(host='0.0.0.0')
```

But, now that we're importing Flask, we have to have it installed.  We could do
that with a `RUN pip3 install flask` command in our Dockerfile, but the
canonical pattern in Python is to list our required libraries in a
`requirements.txt` file, and install them from there.

First, create `requirements.txt`, with the single line: `flask==1.0.2`

Next, copy the `requirements.txt` file into our container's `/app` directory.
We could have a separate `COPY` instruction for that, or we could modify the
`COPY` we already have to just copy everything in our local directory (`.`)
into `/app`.  This is more common.

Now, add a `RUN` instruction that executes `pip3 install -r requirements.txt`

Finally, build the image as before, and run it via:

``` console
$ docker run -d -p 5000:5000 --rm app
```

You should see your container running in the `docker ps` output, and you should
be able to make requests against it using `http :5000`.

## Environment

Let's make our application configurable.  We'll do that [the 12Factor
way](https://12factor.net/config), by embedding configuration as environment
variables.

Change our `root()` method to return the configured color, and add an `import`
statement to our application like such:

``` python
from flask import Flask
from os import getenv
app = Flask(__name__)

@app.route('/')
def root():
    return 'My color is ' + getenv('COLOR', 'clear')

if __name__ == '__main__':
    app.run(host='0.0.0.0')
```

Now build and test your application (you'll have to `docker kill` your running
copy, first).  When you make a request against it, you should see "My color is
clear".

> Notice that every time you update your application codebase, even if you
> didn't modify `requirements.txt`, Docker re-runs the `pip3 install` stage.
> We'll see later why that is (or, better put, why it doesn't re-run _every_
> stage every time), and how to optimize it in another chapter.

Now that our application is expecting configuration through the environment,
let's give it some.  Modify your Dockerfile to set the `ENV` variable `COLOR`
to `red` (or whatever color you fancy).

Build the image, kill the old app, run the new one, and test it like before.
You should see a successful response with your color.

## Using the official image

However, it's rare to install your own Python.  More common is to use one of
the officially supported base images.  As a final step, let's modify our
Dockerfile to use Python version 3.7.3 as a base image, remove all of `apt-get`
instructions, and build/run/test the result.

## Cleaning Up

To clean up, kill your running application, and then remove all stopped
containers and to clean out some disk space by running:

``` console
$ docker system prune --force
```

## Further Reading

[Official Dockerfile Best Practices](https://docs.docker.com/develop/develop-images/dockerfile_best-practices/).

[The birth of `ENTRYPOINT`](https://github.com/moby/moby/issues/1008) (and [the pull request](https://github.com/moby/moby/pull/1086)).

Overly complex but accurate explanation of [how ENTRYPOINT and CMD interact](https://docs.docker.com/engine/reference/builder/#understand-how-cmd-and-entrypoint-interact).

An incorrect and misleading [explanation of what `ENTRYPOINT` is and why it was created](https://stackoverflow.com/questions/21553353/what-is-the-difference-between-cmd-and-entrypoint-in-a-dockerfile/21564990#21564990).

If you don't like the Dockerfile syntax (and you're not alone), then the beta BuildKit support allows you to change it fairly trivially.  [Here's an example using everyone's favorite markup language](https://matt-rickard.com/building-a-new-dockerfile-frontend/).
