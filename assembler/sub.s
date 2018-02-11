.section .data

.section .text

.globl _start

_start:
  pushl $3
  pushl $1
  call subt
  addl $8,%esp
  movl $1, %eax
  int $0x80

.type subt, @function

subt:
  pushl %ebp
  movl %esp, %ebp
  movl 8(%ebp),%eax
  movl 12(%ebp),%ebx
  subl %eax, %ebx

  movl %ebp, %esp
  popl %ebp
  ret
