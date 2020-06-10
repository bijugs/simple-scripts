package main

import "fmt"

func sum(nums ...int) int {
    fmt.Print(nums, " ")
    total := 0
    for _, num := range nums {
        total += num
    }
    return total
}

func main() {

    x := sum(1, 2)
    fmt.Println(x)
    x = sum(1, 2, 3)
    fmt.Println(x)

    nums := []int{1, 2, 3, 4}
    x = sum(nums...)
    fmt.Println(x)
}
