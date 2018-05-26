#!/bin/bash
domain=$1
output=$2
mkdir $output$domain
mkdir $output$domain/data
mkdir $output$domain/log
java -cp target/uber-EntityAnnotation-1.0-SNAPSHOT.jar org.forward.entitysearch.ingestion.HTMLDocumentIngestionManager -i input/$domain.csv -o $output$domain/data/ >> $output$domain/log/std.log 2>> $output$domain/log/err.log &
