.include "record-def.s"
.include "linux.s"

.equ ST_READ_BUFFER, 8
.equ ST_FILE_DES, 12

.section .text
.global read_record
.type read_record, @function

read_record:
  pushl %ebp
  movl %esp, %ebp
  pushl %ebx
  movl ST_FILE_DES, %ebx
  movl ST_READ_BUFFER(%ebp), %ecx
  movl $RECORD_SIZE, %edx
  movl $SYS_READ, %eax
  int $LINUX_SYSCALL

  popl %ebx
  movl %ebp, %esp
  popl %ebp
  ret
