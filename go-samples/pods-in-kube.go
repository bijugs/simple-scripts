package main

import (
  "flag"
  "fmt"
  "github.com/spf13/pflag"
  "k8s.io/apimachinery/pkg/apis/meta/v1"
  "k8s.io/client-go/kubernetes"
  "k8s.io/client-go/tools/clientcmd"
)

func main() {
  // uses the current context in kubeconfig
  var flKubeConfig = pflag.String("kubeconfig", "", "Absolute path to the kubeconfig file")

  pflag.CommandLine.AddGoFlagSet(flag.CommandLine) // support log flags
  pflag.Parse()

  fmt.Printf("%s \n", "Provided KubeConfig")
  fmt.Printf("%s \n", *flKubeConfig)
  // path-to-kubeconfig -- for example, /root/.kube/config
  config, err := clientcmd.BuildConfigFromFlags("", *flKubeConfig)
  if err != nil {
    panic(err)
  }
  // creates the clientset
  clientset, _ := kubernetes.NewForConfig(config)
  namespaces, _ := clientset.CoreV1().Namespaces().List(v1.ListOptions{})
  // access the API to list pods
  for _, namespace := range namespaces.Items {
    fmt.Printf("*** %s ***\n", namespace.Name)
    var totalMemory int64 = 0
    var totalCPU int64 = 0
    pods, _ := clientset.CoreV1().Pods(namespace.Name).List(v1.ListOptions{})
    {
      fmt.Printf("There are %d pods in the Namespace\n", len(pods.Items))
      for _, pod := range pods.Items {
        fmt.Printf("Pod Name=%s Creation Time=%s %s\n", pod.GetName(), pod.GetCreationTimestamp())
        fmt.Printf("Pod Status %s\n",  pod.Status.Phase)
        for _, cont := range pod.Spec.Containers {
          fmt.Printf("Container Name %s Memory=%s CPU=%s\n", cont.Name, cont.Resources.Requests.Memory(), cont.Resources.Requests.Cpu())
          fmt.Printf("Container Limit %s Memory=%s CPU=%s\n", cont.Name, cont.Resources.Limits.Memory(), cont.Resources.Limits.Cpu())
          totalMemory = totalMemory + cont.Resources.Requests.Memory().Value()
          totalCPU = totalCPU + cont.Resources.Requests.Cpu().Value()
        }
      }
    }
    fmt.Printf("Requested Total Memory=%d Total CPU=%d\n", totalMemory, totalCPU)
  }
}
