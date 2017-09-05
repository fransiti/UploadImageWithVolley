<?php 
 
 define('HOST','dttslimitedcom.ipagemysql.com');
 define('USER','imgup');
 define('PASS','123456');
 define('DB','imgup');
 
 $connect = mysqli_connect(HOST,USER,PASS,DB) or die('Db connection failed');
