.section .data

.section .text

.globl _start

_start:
  pushl $1
  pushl $2
  call  add
  addl $8,%esp
  movl $1, %eax
  int $0x80

.type add,@function
add:
  pushl %ebp
  movl %esp,%ebp
  subl $4, %esp

  movl 8(%ebp),%eax
  movl 12(%ebp),%ebx

  addl %eax,%ebx
  movl %ebp,%esp
  popl %ebp
  ret
