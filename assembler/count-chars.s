.equ STRING_ADDRESS, 8
.type count_chars, @function
.globl count_chars
count_chars:
  pushl %ebp
  movl %esp, %ebp
  movl $0, %ecx
  movl STRING_ADDRESS(%ebp), %edx

count_loop:
  movb (%edx),%al
  cmpb $0, %al
  je count_end
  incl %ecx
  incl %edx
  jp count_loop

count_end:
  movl %ecx, %eax
  movl %ebp, %esp
  popl %ebp
  ret
