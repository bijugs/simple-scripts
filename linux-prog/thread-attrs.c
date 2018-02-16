#include <stdio.h>
#include <pthread.h>
#include <unistd.h>

void* tFunction(void* in){
  int old_cancel_state;
  //pthread_setcancelstate(PTHREAD_CANCEL_DISABLE, &old_cancel_state);
  printf("Starting thread function\n");
  sleep(5);
  printf("Ending thread function\n");
  //pthread_setcancelstate(old_cancel_state, NULL);
  return NULL;
}

int main(int argc, char* argv[]){
  pthread_t thread_id;
  pthread_attr_t attr;
  pthread_attr_init(&attr);
  pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_DETACHED);
  pthread_create(&thread_id, &attr, &tFunction, NULL);
  //pthread_cancel(thread_id);
  sleep(10);
  printf("Ending main\n");
  return 0;
}
