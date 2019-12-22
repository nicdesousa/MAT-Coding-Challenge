set -e
bold=$(tput bold)
normal=$(tput sgr0)
blue='\033[1;34m'
log(){
    echo -e "\n<-- ${bold}${blue}$1${normal} -->\n"
}