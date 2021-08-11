# FPL CCD Data Migration Tool

For more info on the tool and how to use it check out https://github.com/hmcts/ccd-case-migration-starter

## Build

To build the project run

```shell
./gradlew clean build
```

this will generate a jar in the `build/libs` directory which can then be used when running the migration.

## Running

To run the jar you will need to do the following

```shell
java -jar \
-Dspring.application.name="fpl-ccd-case-migration-tool" \
-Didam.api.url="https://idam-api.aat.platform.hmcts.net" \
-Didam.client.id="fpl_case_service" \
-Didam.client.secret="[VALUE IN VAULT]" \
-Didam.client.redirect_uri="https://fpl-case-service-aat.service.core-compute-aat.internal/oauth2/callback" \
-Dcore_case_data.api.url="http://ccd-data-store-api-aat.service.core-compute-aat.internal" \
-Didam.s2s-auth.url="http://rpe-service-auth-provider-aat.service.core-compute-aat.internal" \
-Didam.s2s-auth.microservice="fpl_case_service" \
-Didam.s2s-auth.totp_secret="[VALUE IN VAULT]" \
-Dmigration.idam.username="fpl-system-update@mailnesia.com" \
-Dmigration.idam.password="[VALUE IN VAULT]" \
-Dmigration.jurisdiction="PUBLICLAW" \
-Dmigration.caseType="CARE_SUPERVISION_EPO" \
-Dlogging.level.root="ERROR" \
-Dlogging.level.uk.gov.hmcts.reform="INFO" \
-Dfeign.client.config.default.connectTimeout="60000" \
-Dfeign.client.config.default.readTimeout="60000" \
PATH/TO/MIGRATION.jar
```

where

- `idam.client.secret`
- `idam.s2s-auth.totp_secret`
- `migration.idam.password`

can all be found in the fpl-case-service vault.

Note that the parameters given are using AAT environment as an example.

## Common issues

### Running

When running the migration we are making the requests as the system user, as such if the user does not have
[permission](https://github.com/hmcts/fpl-ccd-configuration/blob/master/ccd-definition/AuthorisationCaseField/CareSupervision/system-update.json)
to the fields that it needs to update then the ccd-data-store-api could return errors when validating the case data at
the end of the migration. To ensure that this doesn't occur make sure that the system user has access to all fields
being operated on in both the
[DataMigrationServiceImpl](src/main/java/uk/gov/hmcts/reform/migration/service/DataMigrationServiceImpl.java)
and the
[controller](https://github.com/hmcts/fpl-ccd-configuration/blob/master/service/src/main/java/uk/gov/hmcts/reform/fpl/controllers/support/MigrateCaseController.java)
handling the callback.

### Dependencies

With the deprecation of JCenter the dependencies

```groovy
compile group: 'uk.gov.hmcts.reform.ccd-case-migration', name: 'processor', version: '3.0.0'
compile group: 'uk.gov.hmcts.reform.ccd-case-migration', name: 'domain', version: '3.0.0'
```

are not available to be downloaded, to ensure that you can build and run migrations you can clone
the [stater repo](https://github.com/hmcts/ccd-case-migration-starter) and compile the dependencies locally using

```shell
./gradlew clean build publishToMavenLocal
```
