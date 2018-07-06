#!/bin/bash
for file in `ls $1`; do
    #[ -d "$file" ] || continue
    echo "$file"
    # echo "$1/$file/data/"
    # echo "$1/$file/serializedTitles"
    java -cp target/uber-EntityAnnotation-1.0-SNAPSHOT.jar org.forward.entitysearch.experiment.CreateAnnotatedTitles $1/$file/data/ $1/$file/serializedTitles &
done