curr=`pwd`
reports=("basic" "notbasic")

if [ "$1" = "--upload" ]; then
    echo "Starting repo upload!"
    lein run

elif [ "$1" = "--analyze" ]; then
    for i in ${reports[@]}; do
        if [ "$2" = "$i" ]; then
            echo "Running faunus to analyze the basic properties."        
            cd $BUMI_FAUNUS_DIR
            bin/gremlin.sh -e $curr/src/faunus/$2.groovy
            cd $curr
        fi
    done

elif [ "$1" = "--prettify" ]; then
    for i in ${reports[@]}; do
        if [ "$2" = "$i" ]; then
            echo "Making the basic things pretty with R"
            R CMD BATCH src/R/$2.R
            echo "Checokut the .jpgs in anaysis/people/"                
        fi
    done

elif [ "$1" = "--full-analyze" ]; then    
    for i in ${reports[@]}; do
        if [ "$2" = "$i" ]; then
            ./bumi.sh --analyze $2
            ./bumi.sh --prettify $2
        fi
    done

elif [ "$1" = "help" ]; then 
    cat resources/man.txt
else
./bumi.sh help
fi
