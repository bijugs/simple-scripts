#include <errno.h>
#include <stdio.h>
#include <pthread.h>
#include <stdlib.h>
#include <unistd.h>

pthread_mutex_t mutex_lock = PTHREAD_MUTEX_INITIALIZER;
 
void* thread_function(void* in) {
  if (pthread_mutex_trylock(&mutex_lock) == EBUSY) {
    printf("Lock is already taken %d\n", (int) in);
    pthread_mutex_lock(&mutex_lock); //Threads unlocked in random
  } 
  printf("Got the lock %d\n", (int) in);
  sleep(5);
  printf("Got up from sleep %d\n", (int) in);
  pthread_mutex_unlock(&mutex_lock);
  return NULL;
}

int main(int argc, char* argv[]) {
  pthread_t threads[3];
  int i = 0;
  printf("In main creating threads\n");
  for (i = 0; i < 3; ++i) {
    pthread_create(&(threads[i]), NULL, thread_function, (void *) i);
  }

  for (i = 0; i < 2; i++) {
    pthread_join(threads[i], NULL);
  }

  return 0;
}   
