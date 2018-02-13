#include <stdlib.h>
#include <stdio.h>
#include <sys/types.h>
#include <unistd.h>

int spawn(char* progname, char* argv[]){
  int child_pid = 0;
  child_pid = fork();
  if (child_pid !=0) {
     printf("This is parent process %d\n", (int) getpid());
     printf("Child process id is %d\n", child_pid);
  } else {
     execvp(progname, argv);
     fprintf(stderr,"Error in execvp \n");
     abort();
  }
}

int main(int argc, char* argv[]) {
  int child_status;
  char* args[] = {"ls","-l","/", NULL};
  spawn("ls",args);
  wait(child_status);
  if (WIFEXITED(child_status))
    printf("Child exited fine %d\n", WEXITSTATUS(child_status));
  else
    printf("Child process exited abnormally\n");
  return 0;
}
