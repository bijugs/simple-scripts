#include <stdio.h>
#include <pthread.h>
#include <unistd.h>

pthread_cond_t cond_var;
pthread_mutex_t mutex_lock;
int flag;

void* thread_function(void* in) {
  while (1) {
    pthread_mutex_lock(&mutex_lock);
    if (flag == 0) {
      pthread_cond_wait(&cond_var, &mutex_lock);
      flag = 1;
    } else {
      pthread_cond_wait(&cond_var, &mutex_lock);
      flag = 0;
    }
    printf("Flag unset %d\n",flag);
    pthread_mutex_unlock(&mutex_lock);
  }
}

void* thread_function1(void* in){
  while (1) {
    pthread_mutex_lock(&mutex_lock);
    if (flag == 1)
      flag = 0;
    else
      flag = 1;
    printf("Flag set %d\n",flag);
    pthread_cond_signal(&cond_var);
    pthread_mutex_unlock(&mutex_lock);
    sleep(2);
  }
}

int main(int argc, char* args[]) {
  pthread_mutex_init(&mutex_lock, NULL);
  pthread_cond_init(&cond_var, NULL);
  flag = 0;
  pthread_t thread_1;
  pthread_t thread_2; 
  pthread_create(&thread_1, NULL, thread_function, NULL);
  pthread_create(&thread_2, NULL, thread_function1, NULL);
  pthread_join(thread_1, NULL);
  pthread_join(thread_2, NULL);
  return 0;
}
