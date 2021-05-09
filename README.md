# VMwareAccessOAuth2Demo
VMware Access Authentication with OAuth2.

Prerequisite:  
Admin access to VMware Access environment.

Login to VMWare Access Admin console and create an OAuth2 client of type User Access Token
Pick client id and secret and store them securely.

Specify 'Redirect URI' as http://localhost:8080/verify


Add the details in the file application.properties file


Run the command './gradlew bootRun'
