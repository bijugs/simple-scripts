#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>

void* threadFunction(void* in) {
  while(1)
    fputc('X', stderr);
  return NULL;
}

int main(int argc, char* argv[]){
  pthread_t thread_id;
  pthread_create(&thread_id, NULL, &threadFunction, NULL); // int, thread attributes, thread function, function parameters
  printf("Thread %d created \n",thread_id);
  while(1)
    fputc('o', stderr);
  return 0;
}
