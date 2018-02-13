#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <unistd.h>

int spawn(char* program, char* argv[]){
  pid_t child_id;
  child_id = fork();
  if (child_id != 0)
    return child_id;
  else {
    printf("My id is %d\n",(int) getpid());
    execvp(program, argv);
    fprintf(stderr, "Error in execvp\n");
    abort();
  }
}

int main() {
  char* argv[] = { "ls", "-l", "/", NULL };
  int id = spawn("ls", argv);
  printf("Id returned from spawn %d\n",id);
  return 0;
}
