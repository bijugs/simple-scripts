.section .data

data_items:
  .long 3,6,67,222,2,255

.section .text

.global _start

_start:
  movl $0,%edi
  movl data_items(,%edi,4),%eax
  movl %eax,%ebx
  cmpl $255, %eax
  je loop_end

start_loop:
  incl %edi
  movl data_items(,%edi,4),%eax
  cmpl %ebx, %eax
  jge start_loop

  cmpl $255, %eax
  je loop_end
  movl %eax, %ebx
  jmp start_loop

loop_end:
  movl $1, %eax
  int $0x80
