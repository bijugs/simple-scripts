package main

import (
  "fmt"
  "net/http"
)

func main() {
  	res, err := http.Head("http://artifactory.inf.bloomberg.com/artifactory/libs-release/com/bloomberg/bci/am/FlinkMetricsReporter/1.0.34/FlinkMetricsReporter-1.0.34.jar")
	if err != nil {
		fmt.Println(err)
	}
        fmt.Println(res)
	if res.StatusCode != 200 {
		fmt.Println("Error code")
	}
  	res, err = http.Head("http://artifactory.inf.bloomberg.com/artifactory/libs-release/com/bloomberg/bci/BciFlinkEnrichment/1.1.153/BciFlinkEnrichment-1.1.153-SPAAS.jar")
	if err != nil {
		fmt.Println(err)
	}
        fmt.Println(res)
	if res.StatusCode != 200 {
		fmt.Println("Error code")
	}
}
