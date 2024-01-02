# Build Stage
FROM public.ecr.aws/docker/library/maven:3.8-openjdk-17-slim AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package -DskipTests

# Execution Stage
FROM public.ecr.aws/amazoncorretto/amazoncorretto:17.0.7

# Add app user
ARG APPLICATION_NAME=SalesDataMigrator

ENV APP_DB-URL=$APP_DB_URL
ENV APP_DB-USER=$APP_DB_USER
ENV APP_DB-PASSWORD=$APP_DB_PASSWORD
ENV APP_S3-BUCKET=$APP_S3_BUCKET
ENV APP_S3-KEY=$APP_S3_KEY
ENV AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID
ENV AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY

# Configure working directory
RUN mkdir /app

COPY --from=build home/app/target/$APPLICATION_NAME-1.0-SNAPSHOT.jar /app/app.jar
WORKDIR /app

EXPOSE 8080
ENTRYPOINT [ "java", "-jar", "/app/app.jar" ]