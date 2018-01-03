# abs-excel-converter
This microservice is meant to convert excel sheets in a particular format (see this [issue](http://jira.sysunite.com/browse/RC-77)) to Weaver WriteOperations in JSON format. These WriteOperations can be used to inject the excel structure into a weaver-database.

This is a Maven Java project which exposes a REST API (on a configurable port) to communicate with it. The general concept is to upload an excel file and you get a WriteOperation (batch) returned.

***Version: 0.0.5***

### Configuration
See the **main.properties** file in the **resources** folder and adjust accordingly.

### Usage
To use this product you'll have these routes at your disposal:

##### `[GET] / ` && `[GET] /about `
***Returns***

*Http [200]*

Basic product information.

***Example***
```
{
    "name": "${project.artifactId}",
    "version": "${project.version}",
    "source": "https://github.com/weaverplatform/abs-excel-converter"
}
```
##### `[GET] /connection `
***Returns***

*Http [204]*

Informing the user about the status of the connection.
##### `[POST] /upload `
***Returns***

*Http [200]*

An array containing one or more Weaver WriteOperations in JSON format.

***Example***
```
[{
	<WriteOperation JSON's>
}, ... ]
```

*Http [400]*

When a bad request occured, an invalid ABS Excel sheet was provided. (see the issue
a valid and an invalid example of ABS Excel sheet structures, not the header column).

***Example***
```
{
	"code": 333,
	"message": "Invalid ABS Excel structure."
}
```
