.section .data

  data_items:
    .long 2,3,4

.section .text

.globl _start

  _start:
    movl $data_items, %ebx
    movl $1, %eax
    int $0x80
