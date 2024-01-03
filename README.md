# Sales Data Migrator

![Architecture of service and its place in AWS](Vehicle-Datafill.svg)

This project is designed to build, containerize, and run locally or in aws. 
Its primary inputs are three files that are under the resources directory: firstnames.txt, lastnames.txt, *either* the truncated carvana_car_sold.csv in the resources directory *or* the full csv of 46,000 records held in AWS S3.
These files are merely raw data with no relation to each other, but once executed, this java application 
- reads the 'names' text files and generate 30,000 or another preconfigured amount of Customers. Randomizing pairings for first names, last names, and a customer id. 
- reads the sales csv which contains basic sales information such as vehicle make and sold price and date-- which are then associated with a customer at random and transformed into a true `Transaction`. 
- writes all of the above created data to a postgres database stood up locally through RDS.


The end result is a database populated with vehicles sold and record of a generated customer it was sold to. 

The future intent, through another service is to allow additional customers to be created to bid and purchase vehicles.

### Running Locally
This project leverages docker-compose and localstack to set up a local environment. [Database](database) contains the docker-compose file and an init.sql that contains the schema that should be executed.
The code's resources folders contains a truncated csv of the sales data but this is not what is read by the application. This file should be injected into the S3 localstack container.



>[!Note]
>Thanks to US Census Bureau and Carnegie Mellon University for first and last name raw data pool.
> Carvana Car Sales for publicly providing vehicle sales through the AWS Marketplace
