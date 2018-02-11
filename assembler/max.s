#
# Find max from a list of numbers
#
.section .data

data_items:
    .long 10,5,20,19,33,0

.section .text

.globl _start
_start:
    movl $0, %edi
    movl data_items, %eax
    movl %eax, %ebx

loop:
    cmpl $0, %eax
    je leave
    incl %edi
    movl data_items(,%edi,4),%eax
    cmpl %ebx, %eax
    jle loop
    movl %eax, %ebx
    jmp loop

leave:
    movl $1, %eax
    int $0x80
