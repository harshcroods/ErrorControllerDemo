<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>${errorEntity.statusCode}</title>
</head>
<body>
	<h1 style="text-align: center;"><u>Status Code</u> : ${errorEntity.statusCode}</h1>
	<h1 style="text-align: center;"><u>Message</u></h1>
	<h2 style="text-align: center;">${errorEntity.errorDetails}</h2>
</body>
</html>