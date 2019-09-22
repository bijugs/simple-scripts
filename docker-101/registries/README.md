# Registries

## Cheat Sheet

* `man docker-search`
* `man docker-build`
* `man docker-image-ls`
* `man docker-image-tag`
* `man docker-image-push`

## Pre-Lab Spring Cleaning

Just to make sure we're all cleared out from the last lab, go ahead and run
these two commands:

``` console
$ docker rm -f $(docker ps -aq)
$ docker rmi -f $(docker images -q)
```

We'll leave it to you to figure out exactly what those commands do.

> **Note**: If the `$()` syntax is new to you, no worries.  It's just a bit
> of `bash` kung-foo that takes the output of the command inside `$()` and
> into the surrounding command.  For example, if
>
> ``` console
> $ date +%F
> ```
>
> returns "2019-06-18", then
>
> ``` console
> $ ls logs-(date +%F).tgz
> ```
>
> is the same as
>
> ``` console
> $ ls logs-2019-06-18.tgz
> ```
>
> This is called "command substitution," and you can read more about it via
> `man bash`

## Exploring Docker Hub

### Via the Web Interface

We can't learn about registries without exploring the biggest registry of all:
The Docker Hub.  While the registry API is an open one, and there are many
other registries to choose from, Docker Hub is where the vast majority of third
party images are stored.

Go ahead and point your browser at https://hub.docker.com.  If you're not
logged in (don't worry - we'll get to account creation later), you'll see a
brochure site enticing you to join the fold.  Either way, you'll see an
**Explore** link in the top-right toolbar, and a search box toward the left.

Search for **python**.  On the left-hand side of the search results page, you can
see some checkboxes.  These allow you to make use of some advanced filtering.
Let's narrow our search down to Official Images.

You can see that the official Python repository jumps to the top of the results
list. Go ahead and click into that repo.  Let's walk through this page, as
there's a lot of great content here.

First, the **Description** section contains a full list of the available tags
for this repository.  If you click on any of these versions, you'll be taken to
the Dockerfile that was used to produce that particular image.

Below that, we find a bunch of useful links in the **Quick reference** section,
such as where to file bug reports, lists of supported architectures, etc.

But the real gold is the **How to use this image** section
below.  In here, we find example Dockerfiles and `docker run` commands.

The **Image Variants** section is also full of useful insights.  For example,
you'll see that the recommended `puthon:<version>` images use `buildpack-deps`
as the base image.  We'll learn about efficiently managing image layers later,
but by basing all of their official images on `buildpack-deps`, Docker has
helped us reduce deploy time and disk usage on our workstations and servers.

### For Console Junkies

Now let's make use of the `docker search` command line tool.  This interface is
more powerful in some ways, and less in others.

Use the `docker search` command to find all repos matching the string "py",
filtering to limit to just official repos with more than 500 stars.

While `docker search` allows us to do some fun filtering, it can't show us all
of the useful information that the repo page shows us on Docker Hub. Most
annoying is that we can't list the available tags for a repository.

## Pulling

We can run the `python` image via `docker run`, but this doesn't allow us
to continually pull the latest version.  Remember that tags are mutable, so
`:latest` may point to the same image as `:3.7.3` right now, but it will be
updated to `:3.8.0` once that's been officially released.

> **Note:**  It's for this reason that the general consensus is to _not_ use
> `:latest` for anything other than experimentation.  You'd rather not jump
> major Python versions in production overnight due to your CI or build server
> pulling `:latest`.

To pull the images down to our local image cache, ensuring we get the latest
version, use `docker pull`.

Go ahead and use that to grab `:latest` and `:3.7.3`.  Now use `docker image
ls` to verify you have the images you expect. Remember:  `docker search` is for
searching Docker Hub, while `docker image ls` is for listing your local images.

## Creating Our Repository

We're going to create a Docker Hub user account in order to publish a toy
"moneymaker" application.

> **Note:** If you already have a Docker Hub user account, you're free to use
> that or create a brand new one just for this workshop. If you like, you can
> delete your new repository and even your user account after the lab is
> finished.  We won't be publishing anything sensitive, but what we push _will_
> be publicly visible.

To create a new account, click on the "Sign in" link in the upper right corner.
From there, click "Create account" underneath the sign in form.  You'll need to
provide a username, password and valid email address, and you'll need to
convince the form that you're not a robot.

Once you've submitted the form, verify your new account by clicking the link
emailed to you.  Congratulations -- You're now part of the cloud-native future!

You should now be logged into Docker Hub, and you should see a "Create
Repository" button toward the top of the page.  Click that and create a public
repository named "moneymaker".

You'll see a new repository with no pushed tags.  You'll also see `docker push`
instructions to the right.  We'll be using that example below.

## Login on the Command Line

We've created a new Docker Hub user, but in order to use this account from the
command line, we first have to run `docker login`.  Go ahead and do that now.

## Pushing to the World

We've provided a `Dockerfile` to create a "moneymaker" image.  You can see it
in action by running:

``` console
$ docker run $(docker build -q .)
```

Go ahead and build the image, this time tagging it `moneymaker:v1`.

In order to push a local image to Docker Hub, we first have to tag it with the
fully qualified image name (`docker_id/repo:tag`).  If the Docker ID you
selected when you created your account was `billybob`, then the full image name
for the `:latest` tag of our moneymaker app would be
`billybob/moneymaker:latest`.

> You can double check the fully-qualified image name by looking at the `docker
> push` example command on your new moneymaker repository page on Docker Hub.

Use the `docker tag` command to add the fully-qualified `:v1` tag, and then run
`docker push` with that name to push it up to Docker Hub.

If you browse to the repository page, you'll see your `:v1` tag available for use.

We don't need to have a `:latest` pointer to our most recent image, but people
and tooling generally expect it to be there.  Let's `tag` and `push` a
`:latest` image as well.  We don't want to build a new image - we just want to
point `:latest` at our `:v1` image.

Now when you look on Docker Hub, you'll see both `:v1` and `:latest` available
for use.

## Cleaning Up

Let's clean up our local images by running:

``` console
$ docker rmi -f $(docker images -q)
```

## Further Reading

A [comparison of hosted container
registries](https://caylent.com/container-registries/), and a [comparison of
self-hosted registries](https://www.objectif-libre.com/en/blog/2018/08/02/self-hosted-docker-registries-showdown/).
