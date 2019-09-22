shopt -s checkwinsize
shopt -s histappend
shopt -s no_empty_cmd_completion
shopt -s globstar

export CLICOLOR=1
export HISTCONTROL="ignoredups"
export HISTSIZE="2000"
export LSCOLORS=dxFxCxDxBxegedabagacad
export EDITOR="vim"
export VISUAL="vim"
export PAGER=less
export BLOCKSIZE=K
export LC_CTYPE=en_US.UTF-8
export LESS="-X -M -E -R"
export TERM=xterm-256color
export KUBE_PS1_SYMBOL_COLOR=yellow
export KUBE_PS1_CTX_COLOR=green
export KUBE_PS1_SYMBOL_ENABLE=false
export KUBE_PS1_PREFIX=""
export KUBE_PS1_DIVIDER="["
export KUBE_PS1_SEPARATOR=""
export KUBE_PS1_SUFFIX="] "
export BOTO_CONFIG=/dev/null

export PATH="$HOME/bin:/snap/bin:$PATH"

export KUBECONFIG="$HOME/.kubeconfig"
export HELM_HOME="$HOME/.helm"
export npm_config_loglevel=silent

alias ls="ls -ohF --color=auto"
alias grep="grep --color=auto"
alias ps1='export PS1="\n$ "'

source /etc/bash_completion
source ~/.istioctl.bash
source ~/.docker-compose.bash
source <(stern --completion bash)
source <(kubectl completion bash)
source <(helm completion bash)
source ~/.kube-ps1.sh
[[ -f /snap/google-cloud-sdk/current/completion.bash.inc ]] && \
  source /snap/google-cloud-sdk/current/completion.bash.inc
# https://github.com/docker/docker-ce/tree/master/components/cli/contrib/completion/bash
source ~/.docker-completion.sh

complete -o default -F __start_kubectl k

bash_prompt() {
  local NONE="\[\033[0m\]"
  local    Y="\[\033[0;33m\]"
  local  EMW="\[\033[1;37m\]"
  local  EMY="\[\033[1;33m\]"
  PS1="\$(kube_ps1)${EMW}\$(hostname):$EMY\w$Y $ ${NONE}"
  PS4='$ '
}

bash_prompt
unset bash_prompt

# Get per-project envionment variables from metadata
# shellcheck disable=SC2046
eval $(meta project/attributes | jq -r 'keys[] as $k | select($k|test("^[A-Z_]+$")) | "export \($k)=\"\(.[$k])\""')
