set nocompatible

filetype plugin indent on
syntax on

set listchars=tab:».,nbsp:_,conceal:×
set list
set timeoutlen=1000 ttimeoutlen=10 " Fix esc delay
set mouse=a           " Allow mouse positioning and scrolling in terminal.
set autoread
set backspace=indent,eol,start
set history=1000      " keep 50 lines of command line history
set ruler             " show the cursor position all the time
set incsearch
set hlsearch
set expandtab
set shiftwidth=2
set tabstop=2
set nospell " Disable by default
set completeopt=menu,menuone,preview
set wildmode=longest,list:longest
set signcolumn=yes
set virtualedit=block
set nrformats-=octal
set scrolloff=1
set sidescrolloff=5
set formatoptions+=j
set number
set breakindent
set breakindentopt=shift:0
let &showbreak='↳  '

let &t_SI = "\<Esc>]1337;CursorShape=1\x7"
let &t_EI = "\<Esc>]1337;CursorShape=0\x7"
if &term =~ '256color'
  " disable Background Color Erase (BCE) so that color schemes
  " render properly when inside 256-color tmux and GNU screen.
  " see also http://snk.tuxfamily.org/log/vim-256color-bce.html
  set t_ut=
endif

set background=dark
