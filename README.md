Data loader from internet sites.


Components:
* client  - it loads data through Selenium or HttpClient.
* loadsrv - it saves data to DB.
* linksrv - it saves / gives the links that need to be processed.
	
```no-highlight

            |         |                      |      |       | loadsrv1 |               
            | client1 | -------HTTP------>   |  MQ  |  -->  | loadsrv2 | --> Postgresql
            |         |      save data       | (SQS)|       | ...      | --> DynamoDB  
            | client2 |                      |      |       | loadsrv3 |               
            |         |                                                                 
WEB  --->   | client3 |                                                                 
            |         | 
            | ...     |                      |       |      | linksrv1  |              
            |         | ---HTTP get url--->  | nginx | -->  | linksrv2  | --> Redis    
            | clientN | <--HTTP save url---  |       | <--  | ...       | <--            
            |         |                      |       |      | linksrvN  |                 
```