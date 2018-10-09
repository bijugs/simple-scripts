#include <stdio.h>
#include <sys/shm.h>
#include <sys/stat.h>

int main(int argc, char* argv[]){
  int segment_id; //To store the id returned by shmget
  char* shared_memory; //to store the address returned by shmat
  struct shmid_ds shmdata; //structure to store shm stats
  int segment_size; //To retrieve the size of the segment
  const int shared_segment_size = 0x6400; //size of the shared segment created

  // To get the page size of the OS
  printf("Linux page size %d\n",getpagesize());
  //To create shared memory
  segment_id = shmget(IPC_PRIVATE, shared_segment_size,
                      IPC_CREAT|IPC_EXCL|S_IRUSR|S_IWUSR);
  //To attach the shared memory address to process addressspace
  shared_memory = (char*) shmat(segment_id,0,0);
  printf("Shared memory attached at address %p\n", shared_memory);
  //To get stats of the shm segment
  shmctl(segment_id, IPC_STAT, &shmdata);
  segment_size = shmdata.shm_segsz;  //Size of the allocated shm size
  printf("Segment size: %d\n", segment_size);
  sprintf(shared_memory, "Hello, world.");
  //To detach the shared memory from process address space
  shmdt(shared_memory);
  int child_pid;
  child_pid = fork();
  if (child_pid != 0) {
    printf("This is the parent \n");
    wait(child_pid);
    //To remove shared memory
    shmctl(segment_id, IPC_RMID, 0);
  } else {
    //Attach the shm segment to a particular address in AS
    shared_memory = (char*) shmat(segment_id, (void*) 0x5000000, 0);
    printf("Child with PID %d\n",child_pid);
    printf("%s\n", shared_memory);
  }
  return 0;
}
