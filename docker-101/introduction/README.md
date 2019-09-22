# Introduction!

Welcome to your workstation for the next two days!

This lab will guide you along as you look around.

## Labs

You can see that each lab has a directory and a `README.md` file just like this
one.  First thing you should know is that there are two better ways of reading
these files than `less` or (*gasp*) `cat`:

* You can browse to all of the labs by pointing your browser to
  http://classroom.superorbit.al/pages
* We've included a utility named `bat`, which prints files with syntax
  highlighting and other nice features.  Go ahead and re-open this file using
  `bat README.md`.  This is also useful for viewing YAML and JSON.

The lab directories also have other artifacts (source code, configuration files
and such) that you'll need.  Whenever you work on a lab, you should first `cd`
into that lab's directory and then view the `README.md` file, just like you are
with this one.

Also, don't worry about falling behind -- each chapter starts entirely from
scratch.

## `$HOME`, sweet `$HOME`

`$HOME` is a git repo, which means you can use your `git` commands to snapshot
your progress in case you want to roll back any changes you make.  If you get
well and truly skewered, never fear!  We can completely destroy and recreate
your environment easily (but if we do that, **all of your changes will be
lost**).

Take a look around.  In particular, take a look at the configuration in your
`~/.bashrc` file.  Feel free to add your own.

**Warning:**:  There are some files and directories with unique secrets in them
(`.docker`, `.kubeconfig`, `service-account.json`, `.gcloud`, etc).  Make sure
you don't delete those files.  If you do, we'll have to recreate your user
account, losing all your work 😢

## Tab Completion

We've configured tab-based autocompletion for most of the commands you'll be
using (`docker`, `docker-compose`, `kubectl` and `gcloud`).  For example,
typing `docker <tab>` will complete the available docker commands.  **If in
doubt, hit `<tab>`**.

## Editors

Vim is installed and configured.  Emacs and micro are also there, but without
any extra configuration.  Again, this is your home - make it your own.

If you're not comfortable with command line editors, we've also installed a
web-based IDE, Visual Studio Code.  You can access this IDE by pointing your
browser at your workstation address (`https://N.classroom.superorbit.al`) and
using your student user password.

> **Warning** The IDE even has terminal support built-in, so you can ditch SSH
> entirely.  The web-based IDE is experimental, and may be blocked by some
> corporate firewalls.

## Sudo

This workstation is yours and yours alone.  You also have `sudo` access.  Feel
free to install whatever software you like, but please: No h4cK1ng.
