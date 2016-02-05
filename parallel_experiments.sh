#!/bin/bash
# Argument = -j jar -c configuration -o outputfile -s sshlogin -v verbose

usage()
{
cat << EOF
usage: $0 options

This script runs given jar file with given configurations in parallel locally or on remote server. Arguments in configuration file have to be separated by spaces.

OPTIONS:
   -h			Help
   -j [file]		Jar file*
   -c [file]		Input configurations*
   -o [file]		Output file (stdo if empty)
   -f [file/folder]	Additional file/folder to copy to server
   -s [list]		SSH logins separated by commas (':' for local),
			for n tasks running at once on host use n/login
   -m			Each output to single file
   -v			Progress informations
* required

Examples:

Use two threads on local machine
 parallelize_jar -j exp.jar -o data.out -c config.in -s 2/:

Take over all resources on host PC
 parallelize_jar -j exp.jar -o data.out -c config.in -s user@host

Run one task in parallel on local machine, 4 on hostA and 6 on hostB
 parallelize_jar -j exp.jar -o data.out -c config.in -s 1/:,4/loginA,6/loginB


EOF
}

while getopts “hj:c:o:f:ms:n:v” OPTION
do
     case $OPTION in
	h)
		usage
		exit
		;;
	j)
		JAR_FILE=$OPTARG
		;;
	c)
		CONFIG_FILE=$OPTARG
		;;
	o)
		OUTPUT_FILE=$OPTARG
		;;
	f)
		TRANSFER_FILE=$OPTARG
		;;
	m)
		FILES_ARG="--files"
		;;
	s)
		SSH_LOGIN=$OPTARG
		;;
	v)
		V_ARG="--progress"
		;;
	
	?)
		usage
		exit 1
		;;
     esac
done

if [ -z $JAR_FILE ] || [ -z $CONFIG_FILE ]; then
	usage
	exit
fi

if [ $V_ARG ] && [ -z $OUTPUT_FILE ]; then
	V_ARG=""
fi

if [ $TRANSFER_FILE ]; then
	TRANSFER_ARG="--bf ${TRANSFER_FILE}"
fi

if [ $SSH_LOGIN ]; then
	SSH_ARG="--sshlogin ${SSH_LOGIN} --bf ${JAR_FILE}"
fi

if [ $OUTPUT_FILE ]; then
	parallel --gnu $V_ARG $SSH_ARG $TRANSFER_ARG $FILES_ARG -a $CONFIG_FILE --cleanup --colsep ' ' "java -jar $JAR_FILE" > ./$OUTPUT_FILE
else
	parallel --gnu $V_ARG $SSH_ARG $TRANSFER_ARG $FILES_ARG -a $CONFIG_FILE --cleanup --colsep ' ' "java -jar $JAR_FILE"
fi
