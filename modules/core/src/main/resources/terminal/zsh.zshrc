# https://superuser.com/questions/1027957/zsh-change-prompt-just-before-command-is-run
# https://github.com/pawel-slowik/zsh-async-git-prompt/blob/master/async-git-prompt.plugin.zsh#L237

# setopt prompt_subst # enable command substition in prompt

# https://stackoverflow.com/a/14059262
setopt prompt_subst
setopt prompt_percent

function prompt_cmd() {
    echo "LEFT> "
}

NEWLINE=$'\n'
PROMPT='$(prompt_cmd)' # single quotes to prevent immediate execution
RPROMPT='<<<<<' # no initial prompt, set dynamically
# PROMPT_SUBST=true
INPUT_PIPE_LOCATION="$PWD/prompt_input"
OUTPUT_PIPE_LOCATION="$PWD/prompt_output"
OUTPUT_PIPE_LOCATION2="$PWD/prompt_output2"

mkfifo "$INPUT_PIPE_LOCATION"
mkfifo "$OUTPUT_PIPE_LOCATION"
mkfifo "$OUTPUT_PIPE_LOCATION2"

# kill child if necessary
if [[ "${ASYNC_PROC}" != 0 ]]; then
    kill -s HUP $ASYNC_PROC >/dev/null 2>&1 || :
fi

function shellExit {
    rm -r "$INPUT_PIPE_LOCATION"
    rm -r "$OUTPUT_PIPE_LOCATION"
    rm -r "$OUTPUT_PIPE_LOCATION2"
}

trap shellExit EXIT

ASYNC_PROC=0
function async() {

    # JSON="$(jo type=new_console pid=$$)"
    # echo "$JSON" > "$INPUT_PIPE_LOCATION"


    while true
    do
        OUTPUT=`cat "${OUTPUT_PIPE_LOCATION}"`
        kill -s USR1 $$ || return
        printf "$OUTPUT" > "$OUTPUT_PIPE_LOCATION2"
    done

    # while true
    # do

    #     OUTPUT=`echo -n 'm' | nc -Uq0 /tmp/rust-uds.sock 2>&1`
    #     if [ $? -eq 0 ]; then
    #         printf "$OUTPUT" > "/tmp/zsh_prompt_$$"
    #     else
    #         printf "CANNOT CONNECT TO SERVER\n> " > "/tmp/zsh_prompt_$$"
    #     fi

    #     # signal parent
    #     kill -s USR1 $$ || return

    #     sleep 0.1
    # done

}

function initial_location() {
    JSON="$(jo type=change_dir dir="""$PWD""")"
    echo "$JSON" > $INPUT_PIPE_LOCATION
}

# do not clear RPROMPT, let it persist

# kill child if necessary
if [[ "${ASYNC_PROC}" != 0 ]]; then
    kill -s HUP $ASYNC_PROC >/dev/null 2>&1 || :
fi


# JSON="$(jo type=pid pid=$$)"
# echo $JSON > prompt_input

# start background computation
async &!
ASYNC_PROC=$!

initial_location &!

function TRAPUSR1() {
    # read from temp file
    # RPROMPT="$(cat /tmp/zsh_prompt_$$)"

    # PROMPT="$(cat /tmp/zsh_prompt_$$)${NEWLINE}> "

    # PROMPT="$(cat /tmp/zsh_prompt_$$)"

    # PROMPT=`cat "${OUTPUT_PIPE_LOCATION2}" | xxd`

    # cat ${OUTPUT_PIPE_LOCATION2}

    PROMPT_2=$(< ${OUTPUT_PIPE_LOCATION2})
    PROMPT=$(echo "$PROMPT_2" | openssl enc -d -base64)

    # PROMPT="$(cat "${OUTPUT_PIPE_LOCATION2}")"

    # reset proc number
    # ASYNC_PROC=0
    # PROMPT="${PROMPT} 20%D %*%B%{$fg[red]%}[%{$fg[yellow]%}%n%{$fg[green]%}@%{$fg[blue]%}%M %{$fg[magenta]%}%~%{$fg[red]%}]%{$reset_color%}$%b "


    # set -e

    # redisplay
    # https://unix.stackexchange.com/a/531178
    # https://github.com/Powerlevel9k/powerlevel9k/pull/1176#discussion_r299303453
    zle && zle reset-prompt && zle -R
    # zle && zle reset-prompt
}

del-prompt-accept-line() {
    OLD_PROMPT="$PROMPT"
    OLD_RPROMPT="$RPROMPT"
    PROMPT="> "
    RPROMPT=""
    zle reset-prompt && zle -R
    PROMPT="$OLD_PROMPT"
    RPROMPT="$OLD_RPROMPT"
    zle accept-line
}

zle -N del-prompt-accept-line
bindkey "^M" del-prompt-accept-line

function list_all() {
    JSON="$(jo type=change_dir dir="""$PWD""")"
    # echo "list_all"
    echo "$JSON" > $INPUT_PIPE_LOCATION
}

chpwd_functions=(${chpwd_functions[@]} "list_all")

PROMPT="%F{1} A %F{2} B %F{3} C %F{4} D %F{5} TEST TEST TEST %f>"
# PS1="20%D %*%B%{$fg[red]%}[%{$fg[yellow]%}%n%{$fg[green]%}@%{$fg[blue]%}%M %{$fg[magenta]%}%~%{$fg[red]%}]%{$reset_color%}$%b "
