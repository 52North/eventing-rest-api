# 52°North eventing REST API

Find below example requests for using the API.

## Subscribe

* `POST` to [http://localhost:8080/webapp/v1/subscriptions/](http://localhost:8080/webapp/v1/subscriptions/)

```
{
  "label":"dummy-sub yeah",
  "description":"this subscription is set up!",
  "publicationId":"dummy-pub",
  "templateId":"overshootUndershoot",
  "deliveryMethodId":"email",
  "status":"ENABLED",
  "endOfLife":"2016-06-19T13:22:08.248+02:00",
  "consumer":"peterchen@paulchen.de",
  "parameters":[
    {
      "thresholdValue":55.2
    },
    {
      "observedProperty": "Wasserstand"
    },
    {
      "sensorID": "Wasserstand_Opladen"
    }
  ]
}
```

## Remove subscription

* `DELETE` to [http://localhost:8080/webapp/v1/subscriptions/:id](http://localhost:8080/webapp/v1/subscriptions/:id)
