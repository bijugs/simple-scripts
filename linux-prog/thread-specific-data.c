#include <stdio.h>
#include <string.h>
#include <pthread.h>
#include <errno.h>

static pthread_key_t thread_log_key;

void write_to_log(const char* message) {
  printf("Writing to log \n");
  FILE* file = (FILE *) pthread_getspecific(thread_log_key);
  fprintf(file, "%s\n", message);
}

void close_log(void* thread_log){
  printf("Closing log file\n");
  FILE* file = (FILE*) thread_log;
  fclose(file);
}

void* thread_function(void* in) {
  char log_filename[20];
  FILE* logFile;
  sprintf(log_filename, "thread%d.log", (int) pthread_self());
  printf("log file name %s\n", log_filename);
  logFile = fopen(log_filename,"w");
  if (logFile == NULL) {
    printf("Error opening log file %d %s\n", errno, strerror(errno));
    return NULL;
  }
  pthread_setspecific(thread_log_key, logFile);
  write_to_log("Writing thread ");
  return NULL;
}

int main(int argc, char* argv[]){
  pthread_t threads[5];
  int i;
  pthread_key_create(&thread_log_key, close_log);
  for (i = 0; i < 5; i++) {
    pthread_create(&(threads[i]),NULL,thread_function,NULL);
  }
  for (i = 0; i < 5; i++) {
    pthread_join(threads[i], NULL);
  }
  return 0;
}
