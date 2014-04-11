<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" %>
<html>
<head>
	<title>Home</title>
</head>
<body>
<h1>
	Hello auth!  
</h1>

  Code: ${code} <br>
  State: ${state} <br>
  Error: ${error} <br>
  Access Token: ${accessToken} <br>
  Refresh Token: ${refreshToken} <br>

<P>  The time on the server is ${serverTime}. </P>
</body>
</html>
