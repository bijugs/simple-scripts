#include <stdio.h>
#include <pthread.h>

struct struct_print_chars {
  char c;
  int count;
};

void* print_chars(void* in) {
  printf("Created thread");
  struct struct_print_chars* sPC = (struct struct_print_chars*) in;
  int i = 0;
  for (i = 0; i < sPC->count; ++i) {
    fputc(sPC->c, stderr);
  }
  return NULL;
}

int main(int argc, char* argv[]) {
  pthread_t thread1;
  pthread_t thread2;
  struct struct_print_chars param_1;
  struct struct_print_chars param_2;
  param_1.c = '*';
  param_1.count = 200;
  param_2.c = '=';
  param_2.count = 400;

  pthread_create(&thread1, NULL, &print_chars, &param_1);
  pthread_create(&thread2, NULL, &print_chars, &param_2);
  pthread_join(thread1, NULL);
  pthread_join(thread2, NULL);

  return 0;
}
