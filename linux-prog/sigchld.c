#include <stdio.h>
#include <signal.h>
#include <string.h>
#include <sys/types.h>
#include <unistd.h>

sig_atomic_t child_exit_status;

void cleanup_child_process(int signal_number) {
  int status;
  wait(&status);
  child_exit_status = status;
}

int spawn(){
  int child_pid;
  child_pid = fork();
  if (child_pid != 0)
    return 0;
  else {
    sleep(10);
    printf("Completing child process %d\n",child_pid);
  }
}

int main(int argc, char* argv[]) {

  struct sigaction sigchld_action;
  memset (&sigchld_action, 0, sizeof(sigchld_action));
  sigchld_action.sa_handler = &cleanup_child_process;
  sigaction(SIGCHLD, &sigchld_action, NULL);
  spawn();
  printf("Spawn if child complete\n");
  return 0;
} 
