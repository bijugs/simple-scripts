.section .data
  .equ SYS_OPEN, 5
  .equ SYS_CLOSE, 6
  .equ SYS_READ, 3
  .equ SYS_WRITE, 4
  .equ SYS_EXIT, 1
  .equ LINUX_SYSCALL, 0x80

  .equ O_READ_ONLY, 0
  .equ O_CREATE_TRUNC, 03101

  .equ STDIN, 0
  .equ STDOUT, 1
  .equ STDERR, 2
  .equ EOF, 0
  .equ NO_OF_ARGS, 2

.section .bss
  .equ BUFFER_SIZE, 500
  .lcomm BUFFER_DATA, BUFFER_SIZE 

.section .text
  .equ ST_SIZE_RESERVE, 8
  .equ ST_FD_IN, -4
  .equ ST_FD_OUT, -8
  .equ ST_ARGC, 0
  .equ ST_ARGV_0, 4
  .equ ST_ARGV_1, 8
  .equ ST_ARGV_2, 12

.globl _start
_start:
  movl %esp, %ebp
  subl $ST_SIZE_RESERVE,%esp
  
open_files:
open_fd_in:
  movl $SYS_OPEN, %eax
  movl ST_ARGV_1(%ebp), %ebx
  movl $O_READ_ONLY, %ecx
  movl $0666, %edx
  int $LINUX_SYSCALL

store_fd_in:
  movl %eax, ST_FD_IN(%ebp)
  
open_fd_out:
  movl $SYS_OPEN, %eax
  movl ST_ARGV_2(%ebp), %ebx
  movl $O_CREATE_TRUNC, %ecx
  movl $0666, %edx
  int $LINUX_SYSCALL

store_fd_out:
  movl %eax, ST_FD_OUT(%ebp)

read_loop:
  movl $SYS_READ, %eax
  movl ST_FD_IN(%ebp), %ebx
  movl $BUFFER_DATA, %ecx
  movl $BUFFER_SIZE, %edx
  int $LINUX_SYSCALL
  
  cmpl $EOF, %eax
  jle end

  pushl $BUFFER_DATA
  pushl %eax
  call toupper
  popl %eax
  addl $4, %esp
  movl %eax, %edx
  movl $SYS_WRITE, %eax
  movl ST_FD_OUT(%ebp), %ebx
  movl $BUFFER_DATA, %ecx
  int $LINUX_SYSCALL

  jmp read_loop

end:
  movl $SYS_CLOSE, %eax
  movl ST_FD_OUT(%ebp), %ebx
  int $LINUX_SYSCALL

  movl $SYS_CLOSE, %eax
  movl ST_FD_IN(%ebp), %ebx
  int $LINUX_SYSCALL

  movl $0, %ebx
  movl $SYS_EXIT, %eax
  int $LINUX_SYSCALL

#.type toupper, @function
  .equ LOWERCASE_A, 'a'
  .equ LOWERCASE_Z, 'z'
  .equ CONVERSION, 'A'-'a'
  .equ BUF_LEN, 8
  .equ BUF_ADDR, 12
  
toupper:
  pushl %ebp
  movl %esp, %ebp
  movl BUF_ADDR(%ebp), %eax
  movl BUF_LEN(%ebp), %ebx
  movl $0, %edi
  cmpl $0, %ebx
  je end_toupper

to_upper_loop:
  movb (%eax,%edi,1), %cl
  cmpb $LOWERCASE_A, %cl
  jl next_byte
  cmpb $LOWERCASE_Z, %cl
  jg next_byte

  addb $CONVERSION, %cl
  movb %cl,(%eax,%edi,1)

next_byte:
  incl %edi
  cmpl %edi, %ebx
  jne to_upper_loop

end_toupper:
  movl %ebp, %esp
  popl %ebp
  ret
