.section .data

.section .text

  .globl _start

_start:
  # power(2,3)
  pushl $3   #second parameter
  pushl $2   #First parameter
  call power
  addl $8,%esp
  movl %eax, %ebx #Return value in %eax
  movl $1, %eax
  int $0x80

  .type power,@function
power:
  pushl %ebp
  movl %esp, %ebp
  subl $4,%esp
  movl 8(%ebp), %ebx #First parameter. 8 since call will store the return address in stack
  movl 12(%ebp), %ecx
  movl %ebx, -4(%ebp)

power_loop:
  cmpl $1, %ecx
  je power_end
  movl -4(%ebp), %eax 
  imull %ebx, %eax
  movl %eax, -4(%ebp)
  decl %ecx
  jmp power_loop

power_end:
  movl -4(%ebp),%eax
  movl %ebp, %esp
  popl %ebp
  ret
