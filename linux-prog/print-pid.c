#include <stdio.h>
#include <unistd.h>

int main(int argc, char* argv[]){
  printf("Process id is %d\n",(int)getpid());
  printf("Parent process id is %d\n",(int) getppid());
  return 0;
}
