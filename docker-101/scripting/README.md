# Scripting Docker

## Cheat Sheet

* See the `man docker-container-ls` output, especially the "Filter" and
  "Format" sections.
* `docker ps --format='{{json .}}'`
* `--format="table {{.Foo}}\t{{.Bar}}"`
* `man docker-config-json`

#### Formatting in `{{range}}` statements

If you recall, `{{range .List}} expression {{end}}` statements will render
`expression` for each item, setting `.` to the current item each time.

However, people often get a bit tripped up around the `expression` bit.  There
are two useful things to know:

1. The `expression` includes text and is sensitive to white-space, so:

   ``` handlebars
   {{range .Colors}}Color: {{.Name}}, {{end}}
   ```

   Results in:

   ```
   Color: red, Color: blue, Color: green,
   ```

2. To print a newline, use the overly complicated `{{"\n"}}`. So:

   ``` handlebars
   {{range .Colors}}* {{.Name}}{{"\n"}}{{end}}
   ```

   Results in:

   ```
   * red
   * blue
   * green
   ```

#### `jq` and `gron`

Parsing JSON output can be overwhelming, but there are a couple of command
line tools that can help.

If you just want to pretty print the JSON, then you can use
[jq](https://stedolan.github.io/jq/) like such:

``` console
$ docker ps --format="{{json .}}" | jq
{
  "Command": "\"/tini -- ./color\"",
  "CreatedAt": "2019-08-12 20:37:12 +0000 UTC",
  ...
```

`jq` is _much_ more powerful than this, but it's great as a quick pretty-printer.

If you want to be able to `grep` the JSON output reliably, I like to use a tool
called [gron](https://github.com/tomnomnom/gron). Gron converts JSON into a
one-line-per-value "greppable" form like such:

``` console
$ JSON='[{"a": 1}, {"b": 2}, {"one": {"two": "three"}}]'
$ echo "$JSON" | gron
json = [];
json[0] = {};
json[0].a = 1;
json[1] = {};
json[1].b = 2;
json[2] = {};
json[2].one = {};
json[2].one.two = "three";
```

This makes the JSON structure much easier to understand, which
is useful when crafting `--format` statements.

## Docker Status

We've provided a bash script, `dstats` that totally doesn't work.

The intention is to print out useful information about the containers in our
system.  For all running containers, it should print out the exposed port, and
then all of the mounts.  For all exited containers, it should print the
container name and the exit code.  The final output should look something like
this:

``` console
$ ./dstats
Running:
  red
    Exposed:  0.0.0.0:1234->8000/tcp
    Volumes:
      /home/student/ -> /mnt/home
      /etc -> /host/etc
  blue
    Exposed:  0.0.0.0:4321->5000/tcp
Exited:
  foo exited with code 0
  bar exited with code 23
```

It's your job to fix `dstat`.  We've also provided a script
`setup-containers.sh` that will create a number of containers with
various mounts and exit codes to help with testing.  Run that script,
and then run the `dstats` script to see the current output.  Modify
`dstats` until you get it to run the way we expect.

## Better `docker ps`

As you've probably noticed, the default output of `docker ps` could be better.
Let's see how we can fix that.

Open your `~/.docker/config.json` file.  In this file, you'll see
authentication helpers configured to allow you to `docker push` to your private
GCR registry like such:

``` json
{
  "auths": {},
  "credHelpers": {
    "asia.gcr.io": "gcr",
    "eu.gcr.io": "gcr",
    "gcr.io": "gcr",
    "staging-k8s.gcr.io": "gcr",
    "us.gcr.io": "gcr"
  }
}
```

Alongside this authentication magic, we can add directives to change the output
of many of the `docker` subcommands. Add a key to the main structure named
`"psFormat"`, and set the value to whatever you like.

> **Remember:**  To print a table, you should specify a format like such:
>
> ``` handlebars
> "table {{.Field1}}\\t{{.Field2}}"
> ```
>
> Note the double-backslash in the `\\t` codes in between fields, required
> because of the JSON
> escaping.
> separate fields.

Play around with the various incantations until you find one to your liking.

> **Hint**: If you get this error when you run `docker ps`:
>
> ```
> Error loading config file: /home/student/.docker/config.json:
> invalid character '"' after object key:value pair
> ```
>
> Then you likely forgot a comma in the JSON.

## Cleaning Up

Let's clean up our work by running:

``` console
$ docker rm -f $(docker ps -aq)
$ docker system prune -af --volumes
```

If you like, you can remove the `psFormat` line from your
`~/.docker/config.json` file.

## Further Reading

[A nice collection of aliases and other examples of using `--format` and
`--filter`](http://blog.labianchin.me/2016/02/15/docker-tips-and-tricks)

[Documentation for the Golang template package](https://golang.org/pkg/text/template/), and a shorter bit of [documentation on the Docker `--format` flag](https://docs.docker.com/config/formatting/)


[How to configure the `docker ps` output](https://container42.com/2016/03/27/docker-quicktip-7-psformat/)

Documentation on [configuring your `~/.docker/config.json` file](https://docs.docker.com/engine/reference/commandline/cli/#configuration-files)


A very [in-depth tutorial on advanced Go-template usage with
Docker](https://blog.container-solutions.com/docker-inspect-template-magic)
