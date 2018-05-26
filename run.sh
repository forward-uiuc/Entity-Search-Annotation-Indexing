#!/bin/bash
domain=$1
mkdir /srv/local/ltpham3/esdoc/$domain
mkdir /srv/local/ltpham3/esdoc/$domain/data
mkdir /srv/local/ltpham3/esdoc/$domain/log
# java -cp target/uber-EntityAnnotation-1.0-SNAPSHOT.jar org.forward.entitysearch.ingestion.HTMLDocumentIngestionManager -i input/$domain.csv -o /srv/local/ltpham3/esdoc/$domain/data/ >> /srv/local/ltpham3/esdoc/$domain/log/std.log 2>> /srv/local/ltpham3/esdoc/$domain/log/err.log &
