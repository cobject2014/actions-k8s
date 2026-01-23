1. Build Java project
2. Build container for this java project
3. Deploy java and Redis to different Pod (Handle both local ACT env and cloud Github Actions env)
4. write a test script to make core test, which do following steps
   # 4.1 Send serveral request to Echo server and check 
   # 4.2 echo resposne is correct
   # 4.3. Redis is updated 