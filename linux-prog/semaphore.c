#include <stdio.h>
#include <pthread.h>
#include <semaphore.h>
#include <unistd.h>

pthread_mutex_t mutex_lock = PTHREAD_MUTEX_INITIALIZER;
sem_t barrier;

void* thread_function(void* in) {
  printf("Will wait on barrier %d\n", (int) in);
  sem_wait(&barrier);
  printf("Crossed the barrier %d\n", (int) in);
  pthread_mutex_lock(&mutex_lock);
  printf("Took the lock %d\n", (int) in);
  sleep(5);
  pthread_mutex_unlock(&mutex_lock);
  return NULL;
}

int main(int argc, char* argv[]) {
  sem_init(&barrier, 0, 0);
  pthread_t threads[5];
  int i = 0;
  for (i = 0; i < 5; i++) {
    pthread_create(&(threads[i]), NULL, thread_function, (void*) i);
  }
  sem_getvalue(&barrier, &i);
  printf("Number of sem value %d\n", i);
  for (i = 0; i < 5; i++)
     sem_post(&barrier);
  for (i = 0; i < 5; i++)
    pthread_join(threads[i],NULL);
  return 0;
}
