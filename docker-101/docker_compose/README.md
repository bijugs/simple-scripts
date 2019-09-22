# Docker Compose

In this lab we're going to compose a single application, make use of the "Bind
Mount Over `/app`" pattern, and then expand the compose configuration to
include a database.

## Cheat Sheet

* [`docker-compose.yaml` documentation](https://docs.docker.com/compose/compose-file).  In particular, the sections on:
    * [`services.*.ports`](https://docs.docker.com/compose/compose-file/#ports)
    * [`services.*.volumes`](https://docs.docker.com/compose/compose-file/#volumes)
    * [`services.*.environment`](https://docs.docker.com/compose/compose-file/#environment)
* [`docker-compose` command line documentation](https://docs.docker.com/compose/reference/)
* `docker-compose help up`
* `docker-compose up --build`

## Starting Fresh

Let's clean up our work from our previous lab by running:

``` console
$ docker rm -f $(docker ps -aq)
```

## Basic Composition

In this directory, you'll see a `docker-compose.yaml` file and a `frontend`
directory with an `app.py` and a `Dockerfile`.  Take a look at the `app.py` and
`Dockerfile` files to make sure you understand how they work.

Next, edit the `docker-compose.yaml` file.  Under the `services` section, define
a service named `frontend` that builds the `frontend` directory, and that
forwards port 80 to port 8000 on the container.  Use the documentation for help.

When you're done, run `docker-compose up --build` and observe your container
starting. Use your local web browser to connect to your workstation
(`http://N.classroom.superorbit.al`) and see the "Hello World" message.  Also
observe your connection being logged in the `docker-compose up` output.

> `docker-compose up` will build your images _the first time_, but it will
> happily ignore any changed files in those directories when you run it again
> later.  It's always best to use the `--build` flag to force Compose to
> rebuild the images.  The normal Docker caching mechanisms still work, so the
> rebuild times are often miniscule.

With the `docker-compose up` still running, open a new terminal on your
workstation.  In that terminal use the normal `docker` command to list the
running containers.  What do you see?  Where are the container names coming
from?

## Bind Mounts

Docker Compose is almost universally used for local development.  Let's use the
Bind Mount trick we discussed previously to make local development faster and
easier on our fingers.

Kill the `docker-compose up` command either by typing `Ctrl-C` in that window,
or by typing `docker-compose down` in another terminal.

Now, edit the `docker-compose.yaml` to add a bind mount volume to the
`frontend` service.  This volume should mount the `frontend/` directory into
`/app` on the container.  This will allow us to modify the application without
needing to recreate the container images.

> **Note:** There are two places you can specify volumes in the
> `docker-compose.yaml` file: at the service level and globally.  Global
> volumes are akin to `docker volume create`, and we _won't_ be using them.

However, this is a Flask application, so we also need to configure it to reload
changed Python files on each request.  To do this, add an environment variable
(again, in the `frontend` service in your `docker-compose.yaml` file), setting
`FLASK_DEBUG=1`.

Finally, `docker-compose up --build`, and use your other terminal to change the
greeting that `app.py` presents us.  Refresh your browser, and observe the new
greeting!

## Add a Database

This is all well and good, but not a huge step forward from just using the
stock Docker commands.   Compose really shines when it's used to manage many
services that work in concert (pun fully intended).

First, let's modify the `docker-compose.yaml` file to launch another service.
Name this one `redis`, and have it run the `redis:5` image.

> You don't need to specify any ports, since the `frontend` application will be
> connecting to it directly over the custom network that Compose configures by
> default.

Kill the currently running `docker-compose up` and re-run it with the modified
configuration.  You should see both the `frontend` and `redis` containers
running.

Next, open the `frontend/app.py` file and modify it to connect to `redis`:

1. Add an `import redis` line after the Flask import.
2. Add a line under the `app = Flask...` that reads like such:

   ``` python
   cache = redis.Redis(host='redis', port=6379)
   ```

3. Change the `root()` function.  Replace the `return "greeting"` with:

   ``` python
   count = cache.incr('hits')
   return f'I have been seen {count} times.\n'
   ```

We've already installed the `redis` python package in the Dockerfile, so no
need to rebuild your image.

Refresh your browser, and you should see the grammatically questionable "I have
been seen 1 times."  Refresh your browser again, and you should see the count
increase.

## Cleaning Up

Let's clean up our work by running:

``` console
$ docker rm --force $(docker ps -aq)
```
