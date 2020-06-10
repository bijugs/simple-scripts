package main

import (
   "fmt"
   "time"
)

func main() {

    messages := make(chan string, 2)

    go func(c chan string) {
       c <- "buffered"
       fmt.Println("Wrote 1")
       c <- "channel"
    } (messages)

    time.Sleep(10 * time.Second)
    fmt.Println(<-messages)
    fmt.Println(<-messages)
}
