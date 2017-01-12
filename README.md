# SuperDuo
SuperDuo Project for UDACITY course.
 - Football Scores
The server impose a limit of 50 request per minute. When downloading the team icons new request to the api are made and each match represents 2 new requests to get the crests. That way is very easy to reach the 50 request per minute limit if we donwload the crests of all the teams in all leagues at one time. To avoid this problem i have created a Settings activity where the user can choose between several leagues avoid that fetching many crests at one time. It is the unique way i found to solve the server limit. 

API KEY=fee3c081f9aa4127a41bcea9fde7f6f5 , in xml it is situated below "about text"
