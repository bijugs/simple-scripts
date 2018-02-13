#include <stdio.h>
#include <sys/types.h>
#include <unistd.h>

int main() {

  pid_t child_pid;
  printf("main processes pid is %d\n",(int) getpid());
  child_pid = fork();
  if (child_pid != 0) {
    printf("This is parent process %d\n", (int) getpid());
    printf("Child process PID is %d\n", child_pid);
  } else {
    printf("This is child process %d\n", (int) getpid());
  }
  return 0;
} 
