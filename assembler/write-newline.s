  .include "linux.s"
  .globl write_newline
  .type write_newline, @function
  .section data
newline:
  .ascii "\n"
  .section .text
  .equ ST_FILE_DES, 8
write_newline:
  push %ebp
  movl %esp, %ebp

  movl $SYS_WRITE, %eax
  movl ST_FILE_DES(%ebp), %ebx
  movl newline, %ecx
  movl $1, %edx
  int $LINUX_SYSCALL
  movl %ebp, %esp
  popl %ebp
  ret
