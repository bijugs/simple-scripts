.include "linux.s"
.include "record-def.s"

.section .data
record1:
  .ascii "Frederick\0"
  .rept 30
  .byte 0
  .endr

  .ascii "Bartlett\0"
  .rept 31
  .byte 0
  .endr

  .ascii "1234 Test\0"
  .rept 230
  .byte 0
  .endr

  .long 45

record2:
  .ascii "Micheal\0"
  .rept 32
  .byte 0
  .endr

  .ascii "Forrester\0"
  .rept 30
  .byte 0
  .endr

  .ascii "1234 Test\0"
  .rept 230
  .byte 0
  .endr

  .long 50

file_name:
  .ascii "test.dat\0"

  .equ ST_FILE_DESCRIPTOR, -4

  .globl _start

_start:
  movl %esp, %ebp
  addl $ST_FILE_DESCRIPTOR, %esp

  movl $SYS_OPEN, %eax
  movl $file_name, %ebx
  movl $0101, %ecx
  movl $0666, %edx
  int $LINUX_SYSCALL

  movl %eax, ST_FILE_DESCRIPTOR(%ebp)

  pushl ST_FILE_DESCRIPTOR(%ebp)
  pushl $record1
  call write_record
  addl $8, %esp

  pushl ST_FILE_DESCRIPTOR(%ebp)
  pushl $record2
  call write_record
  addl $8, %esp

  movl $SYS_CLOSE, %eax
  movl ST_FILE_DESCRIPTOR(%ebp), %ebx
  int $LINUX_SYSCALL

  movl %ebp, %esp
  popl %ebp

  movl $SYS_EXIT, %eax
  movl $0, %ebx
  int $LINUX_SYSCALL
