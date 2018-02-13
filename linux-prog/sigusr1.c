#include <signal.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <string.h>
#include <unistd.h>

sig_atomic_t sigusr1_count = 0;

void handler(int signal_number){
  ++sigusr1_count;
}

int main() {
  struct sigaction sa;
  memset(&sa, 0, sizeof(sa));
  sa.sa_handler = &handler;
  while (1) {
    sleep(1);
    if (sigusr1_count == 10)
      break;
  }
  printf("SIGUSR1 is raised %d times \n",sigusr1_count);
  return 0;
}
