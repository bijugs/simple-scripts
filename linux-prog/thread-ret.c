//
// Threads returns something
//
#include <stdio.h>
#include <pthread.h>

struct numberCollection {
  int count;
  int* num;
};

void* largestNumber(void* in){
  int ret = 0;
  struct numberCollection* nos = (struct numberCollection*) in;
  int i = 0;
  for (i = 0; i < nos->count; i++) {
    if (nos->num[i] > ret)
      ret = nos->num[i];
    printf("Print %d no %d ret %d\n",i,nos->num[i],ret); 
  }
  return (void*) ret; //This cast is ugly
}

int main(int argc, char* args[]){
  int lNo = 0;
  int nos[5] = {2,1,4,6,5};
  pthread_t t_pid;
  struct numberCollection collect;
  collect.count = 5;
  collect.num = nos;
  pthread_create(&t_pid, NULL, &largestNumber, &collect);
  if (!pthread_equal(pthread_self(), t_pid))
    pthread_join(t_pid,(void *)&lNo);
  printf("Largest number %d\n", lNo);
  return 0;
}
