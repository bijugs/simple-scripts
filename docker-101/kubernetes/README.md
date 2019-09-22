# Intro to Kubernetes

## Cheat Sheet

* `kubectl get --help`
* `kubectl apply --help`
* `kubectl scale --help`
* `kubectl rollout undo --help`
* `kubectl delete --help`

## Kick the Tires

If you didn't do so at the beginning of the chapter, then go ahead an run
`lab-cluster create` now.  This script provisions a cluster of virtual machines
in Google Cloud running Kubernetes.  You have your own dedicated cluster to
work with, and you can recreate the cluster from scratch via `lab-cluster
recreate` (and wait 10 minutes for it to finish) if you ever need to.

Before we begin, kick the tires of your cluster by running:

``` console
$ kubectl get componentstatuses
```

> We've configured tab-completion, so you should only need to type `kubectl
> get com<tab>` to fill in the entire line.

This command shows the health of the main
Kubernetes components: The main controller, the scheduler, and the
Etcd cluster used for all API storage.  You should see "Healthy"
across the board.

Next, let's look at the virtual machine Nodes that will be running our
containers.  Run:

``` console
$ kubectl get nodes -o wide
```

You should see three Nodes listed, the OS image they're running, and the kernel
versions.

> **Note:** The `-o` flag is short for `--output`, and can be given a number of
> different options.  The most commonly used ones are:
>
> * `wide`, which adds extra useful columns,
> * `yaml`, which returns the results as YAML,
> * and `json`, which returns JSON.

## Create a Unicorn

One of the great benefits of Kubernetes is that it works well as a declarative
and idempotent system.  This means we can declare our desired workloads in an
object definition file (sometimes called a "manifest") and apply that
configuration as many times as we like without fear of side-effects.

We've provided a manifest for your use, named `instant-unicorn.yaml` (🦄💰🍾)

Look through this file. You won't need to understand the bulk of this file in
order to complete the lab, but it's useful to understand that it describes
four objects (each separated by a single line containing `---`): A frontend
Deployment and Service and a backend Deployment and Service.

> See the Further Reading section below if you'd like a quick primer on the
> YAML syntax.

Now we're going to hand this manifest to Kubernetes using the `kubectl apply`
command like such:

``` console
$ kubectl apply -f instant-unicorn.yaml
```

This will create all four of these objects.  Go ahead and run the command
again, and note that the output says "unchanged."  **Idempotency in action,
baby!**

Let's check that everything deployed correctly.  Go ahead and list your
deployments via:

``` console
$ kubectl get deployments
```

Recall that a Deployment exists to manage Pods (which in turn run our actual
containers).  You can see that each Deployment is configured to run a single
Pod via:

``` console
$ kubectl get pods
```

Finally, our manifest deployed the `frontend` service as a cloud
load-balancer.  This means it will have a public IP address, and can be
accessed from the big bad Internet.  Let's find out what that address is by
running:

``` console
$ kubectl get service frontend
```

We're looking for the value under the `External IP` column.  If you see
`<pending>`, that just means that The Cloud™️ hasn't finished provisioning the
load-balancer.  Simply wait a couple minutes and repeat until you see an IP
address.

Once you have that IP address (mine is `35.193.149.228`, but yours will be
different), point your laptop browser at `http://35.193.149.228/backend`, and
you should see something like:

```
Hello! I'm: version: v8
```

> The frontend service just proxies to other internal services by mapping the
> path you request to the internal hostname.  When we get `/backend`, it will
> proxy the request to the `backend` service and return the response.  This
> works because Kubernetes provides an internal DNS system that routes to the
> right Service in a similar way to how Docker user-defined networks work.

## Keep an Eye Out

For the next few sections it's going to be useful to be constantly watching the
output of `kubectl get pods`.  Open a new terminal window alongside your main
one, and run the following command:

``` console
$ watch -n 1 kubectl get pods
```

This will print the status of all Pods, once every second.

## Roflscale!

Black Friday approaches!  We need to scale the backend from 1 to 30 as fast as
possible!

Not a problem.  Run the following command:

``` console
$ kubectl scale deployment backend --replicas=30
```

Observe the `watch` command in your other terminal.  How long did it take for
the Deployment to provision 30 healthy Pods?  **Welcome to The Cloud!**

## Heal Thyself!

Kubernetes is designed to be self-healing.  We can test this by deleting one of
the Pods by hand and watching the Deployment bring a new one in to take its
place.

Run the following to delete a single pod (pick any of your Pods -
tab-completion is your friend.). Keep a close eye on your `watch` window while
you run this command:

``` console
$ kubectl delete pod YOUR_POD_NAME
```

How quickly did the new Pod replace the old?  **Welcome to the world of
declarative infrastructure!**

## There and Back Again

Finally, it's time to roll out a new version of our backend.  Instead of
copying the new application code to each of the "servers," or manually updating
each Pod by hand, we're just going to update our manifest and re-apply.

Edit `instant-unicorn.yaml`, and find the
place where we specify that we want to deploy the `v8` version of the
`superorbital/example` image.  Change that to deploy `v9`, and the run `kubectl
apply -f instant-unicorn.yaml` again.  What output do you see?

Look over in your `watch` window.  How long does it take for the entire
Deployment to converge to a healthy state?  This takes longer than when you
first scaled out because Kubernetes is performing a graceful rollout, and our
manifest says we only want to update 10% of the Pods at a time.

Notice that the Deployment now consists of entirely new Pods.  This is
Kubernetes practicing **Immutable Infrastructure!**

Refresh your browser window, and you should see

```
Hello! I'm: version: v9
```

**BUT wait!**  There was a critical security vulnerability in `v9` and we need
to roll back to good ol' `v8`!  _Oh `v8`, why did we ever leave you?!?_

Normally we'd just update the manifest (changing `v9` back to `v8`), and let
our CI/CD system do its work.  However, there's no time to waste with a fix
this critical.  Let's break out the `rollout undo` command.  Run the following
and observe your Deployment recreate the Pods once again:

``` console
$ kubectl rollout undo deployment backend
```

Refresh your browser window, and you should see version 8's sunny smile!

## Cleaning Up

Let's clean up our work by running:

``` console
$ kubectl delete -f instant-unicorn.yaml
$ lab-cluster destroy
```

## Further Reading

An absolutely brilliant [comic about Kubernetes with a bonus interactive
playground at the
end](https://cloud.google.com/kubernetes-engine/kubernetes-comic/)

A quick [introduction to YAML as used by
Kubernetes](https://www.mirantis.com/blog/introduction-to-yaml-creating-a-kubernetes-deployment/).

The [official Kubernetes documentation](https://kubernetes.io/docs/home/)
