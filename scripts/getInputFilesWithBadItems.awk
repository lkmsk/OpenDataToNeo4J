#!/usr/bin/awk 

BEGIN 
  { 
    FS=":"
  } 
  /.dsv:/ 
  { 
  print $2 
  }
