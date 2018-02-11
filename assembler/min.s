#
# Find the minimum
#

.section .data
  data_array:
    .long 2,5,7,1,9
  data_length:
    .long 4

.section .text

.globl _start

_start:
  movl data_length, %ecx
  movl $0, %edi
  movl data_array, %eax
  movl %eax, %ebx

loop:
  incl %edi
  cmpl %ecx, %edi
  jg leave
  movl data_array(,%edi,4), %eax
  cmpl %ebx, %eax
  jge loop
  movl %eax, %ebx
  jmp loop

leave:
  movl $1, %eax
  int $0x80    
