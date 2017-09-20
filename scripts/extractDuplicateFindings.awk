#!/usr/bin/awk
BEGIN {
print "{"
}
NF {
  id=$2
  src1=$13
  src2=$15
  print "\"bad\": {\"duplicateID\": \"" $2 "\", \"src-1\": \"" $13 "\", \"src-2\": \"" $15 "\"},"
#    sub(/\r/, "");
#    gsub(/\"/, "\\\"");
#    printf "%s", "\"data\":\"";
#    printf "%s", $0;
#    print "\"}";
}
END {
print "}"
}
