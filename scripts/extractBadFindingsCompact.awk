#!/usr/bin/awk
BEGIN {
print "{"
}
NF {
  startWord=$1
  id=$2
  item1=$3
  item2=$4
  item3=$5
  item4=$6
  item5=$7
  item6=$8
  item7=$9
  item8=$10
  duplicateItem1 = $13
  duplicateItem2 = $15
  descriptionPrefix = $3 " " $4 " " $5 " " $6 " " $7 " " $8
  badRelationshipDescription = $1 " " $2 " " $3 " " $4 
############################################################
# InputRelationship:
#   source: /opt/developers/aktuell/graphdb/dataimport/import_dsv/AST_3_to_SK_907.dsv:2
#   startNode: 1030728202001 (GOBJ-ID)
#   endNode: 907 (Subkontext-ID)
#   type: IN_SUBKONTEXT
# referring to missing node 907
############################################################
# "is defined more than once in"
############################################################
  if(descriptionPrefix ~ "is defined more than once in") {
    print "\"bad\": {\"Type\": \"Duplicate-ID\", \"NS\": \"" $9 "\", \"ID\": \"" $2 "\"},"
  } else if(startWord ~ "InputRelationship:") {
    print "\"bad\": {\"Type\": \"Broken-Link\""
  } else if(startWord ~ "source:") {
#    print ", \"Relationship-Item\": \"" $2 "\""
  } else if(startWord ~ "startNode:") {
#    print ", \"Relationship-Start-ID\": \"" $2 "\", \"Start-NS\": \"" $3 "\""
  } else if(startWord ~ "endNode:") {
    print ", \"ID-NS\": \"" $3 "\""
  } else if(startWord ~ "type:") {
    print ", \"Relationship-Type\": \"" $2 "\""
  } else if(badRelationshipDescription ~ "referring to missing node") {
    print ", \"Broken-ID\": \"" $5 "\"},"
  }
#    sub(/\r/, "");
#    gsub(/\"/, "\\\"");
#    printf "%s", "\"data\":\"";
#    printf "%s", $0;
#    print "\"}";
}
END {
print "}"
}
