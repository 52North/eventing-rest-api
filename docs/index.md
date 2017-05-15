# 52°North Eventing REST API

The 52°North Eventing REST API is a lightweight management API for
providing publish/subscribe functionality within the Sensor Web domain. It is
inspired by the
[OGC Publish/Subscribe 1.0 specification](http://www.opengeospatial.org/standards/pubsub),
but does not follow its definitions 100%. This document provides an overview
on the API, its configuration as well as  installation/deployment information.

## Technology

The API comes as a Web Application Container and can be served from any
compatible Application Server (Jetty, Tomcat, ...). Java 8 or higher is
required. The foundation of the architecture is the Spring framework.

### Current Limitations

The API is still in the development phase and comes with a few known
limitations:

* No persistent subscriptions. Subscriptions will be lost on restart of the
web application
* limited amount of built-in delivery methods

## Installation

Apache Maven (>= 3.3x) is required to build the application.
To create a deployable web archive, simply run the following command in the root
directory:

`mvn clean install`

After successful execution, a `war` file is available in the `webapp/target`
directory: `eventing-rest-api.war`. This file can be deployed to the
Application Server of your choice via the corresponding methods.

## Configuration

By default, you are not required to adjust the configuration. Still, there
are some built-in connectors for delivery methods and publications that require
some configuration.

The configuration file can be found at the following location:

`<application-server-webapps>eventing-rest-api/WEB-INF/classes/config.json`

The webapp has to be restarted to reflect configuration changes.

### MQTT Delivery

The following parameters are required to enable the MQTT delivery method:

| Parameter | Type | Example |
|-----------|------|---------|
| `delivery.mqtt.host` | `string` | `mydomain.com`, `123.456.789.1` |
| `delivery.mqtt.port` (optional) | `integer` | `1883`, default: `1883` |
| `delivery.mqtt.protocol` (optional) | `string` | `tcp`, default: `tcp` |

A subscriber can define a desired target topic where matching messages should
be delivered. If it is not provided, the API creates a unique random topic.

### MQTT Publication

The API comes with a built-in component to ingest data from an MQTT resource
as a publication.
The following parameters are required to enable the MQTT publication:

| Parameter | Type | Example |
|-----------|------|---------|
| `connector.mqtt.host` | `string` | `mydomain.com`, `123.456.789.1` |
| `connector.mqtt.publicationIdentifer` | `string` | `my-mqtt-data-publisher` |
| `connector.mqtt.port` (optional) | `integer` | `1883`, default: `1883` |
| `connector.mqtt.protocol` (optional) | `string` | `tcp`, default: `tcp` |
| `connector.mqtt.topic` (optional) | `string` | `station-abc/temperature`, default: `#` |
| `connector.mqtt.mimeType` (optional) | `string` | `application/json`, `text/csv` |

The publicationIdentifer is required in order to provide a unique identifier
for referencing in a subscription.

### Configuration Example

An example for a configration featuring both MQTT delivery and publication
could look like the following:

```json
{
    "templateDirectory": "/templates",
    "connector.mqtt.protocol": "tcp",
    "connector.mqtt.host": "test.mosquitto.org",
    "connector.mqtt.port": 1883,
    "connector.mqtt.mimeType": "application/json",
    "connector.mqtt.publicationIdentifer": "test-mqtt-data",
    "connector.mqtt.topic": "n52/eventing",
    "delivery.mqtt.host": "test.mosquitto.org"
}

```

In order to not stress the [mosquitto](https://mosquitto.org/) test server,
*please use this configuration for testing purposes only*!

## API Usage

The basic resource of the API is available at the following URL (given that
the Application Server is running on port `8080` of `localhost`):

http://localhost:8080/eventing-rest-api/v1

The API provides navigable URLs within the response of the root. It is
suggested to install a browser extension that supports the rendering of JSON
(e.g. *JSONView* for Firefox or Chrome).

## Subscription Management

Subscription management is provided via the `/subscriptions` sub-resource.

### Subscribe operation

`HTTP POST` with *Content-Type* `application/json` against
http://localhost:8080/eventing-rest-api/v1/subscriptions:

```json
{  
   "label":"My first subscription",
   "publicationId":"an-mqtt-publisher",
   "deliveryMethods":[{  
      "id":"mqtt-delivery",
      "parameters":{  
         "topic":{  
            "value":"my-target-topic"
         }
      }
   }],
   "enabled":true,
   "endOfLife":"2017-06-19T13:22:08.248+02:00"
}
```

The response provides the subscription ID and a URL that can be used to
manage the subscription.

The `publicationId` parameter has to match a publication as provided by the
`publications` resource at
http://localhost:8080/eventing-rest-api/v1/publications.

The `id` parameter of an object in the `deliveryMethods` array has to match
a delivery method as provided by the `deliveryMethods` resource at
http://localhost:8080/eventing-rest-api/v1/deliveryMethods.

### Subscription details

Details of a subscription resource can be examined at the following URL:
http://localhost:8080/eventing-rest-api/v1/subscriptions/:subscriptionId

### Delete operation

`HTTP DELETE` against
http://localhost:8080/eventing-rest-api/v1/subscriptions/:subscriptionId

### Renew operation

`HTTP POST` with *Content-Type* `application/json` against
http://localhost:8080/eventing-rest-api/v1/subscriptions/:subscriptionId:

```json
{
  "endOfLife": "2017-07-19T13:22:08.248+02:00"
}
```

This will result in the extension of the termination time.
