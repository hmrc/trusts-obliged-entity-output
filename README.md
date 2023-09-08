# Trusts obliged entity output

This service is responsible for fetching the information about the persons in the trust from API#1584 Get Trust Obliged Entities and generating a PDF from this data with the NRS PDF service.

This service acts as a proxy to IF and NRS. The information is not transformed/modified in any way.

### Running

To run locally using the micro-service provided by the service manager:

```
sm2 --start TRUSTS_ALL
```


If you want to run your local copy, then stop the frontend ran by the service manager and run your local code by using the following (port number is 9780 but is defaulted to that in build.sbt).

`sbt run`

### Testing
To test the service locally run use the following script, this will run both the unit and integration tests as well as check for dependency updates and check the coverage of the tests.

`./run_all_tests.sh`

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
