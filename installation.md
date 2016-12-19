# Installation der Eventing REST API

## Voraussetzungen

Die API ist in Java entwickelt und muss in einem Application Server installiert
werden. Folgende Voraussetzungen müssen erfüllt sein:

1. Java 8 JVM (Orcale oder OpenJDK)
1. Application Server (z.B. Tomcat ab Version 7)
1. Zugriff auf die PostgreSQL-Datenbank

## Installation

Die API kann wie jede andere Webapp im Tomcat installiert werden (z.B. Kopie in
  den Order `<tomcat-dir>\webapps`, Hochladen via Tomcat Manager). Danach
  muss die Konfiguration angepasst werden.

## Konfiguration

### Anpassung der Datenbank-Verbindung

Die Verbindung zur Datenbank wird in der Datei
`<tomcat-dir>\webapps\eventing-rest-api\WEB-INF\classes\wv\hibernate-eventing.cfg.xml`
definiert.

Der Eintrag `<property name="connection.url">` muss entsprechend angepasst
werden. Er folgt dabei diesem Schema:

`jdbc:postgresql://<host>:<port>/<db-name>?currentSchema=<db-schema>`

Also z.B.:

`jdbc:postgresql://localhost:5432/wv-eventing?currentSchema=sensorweb2`

### Konfiguration der Berechtigungen

Die Berechtigungen für die Nutzung der API werden per Gruppenzuweisungen
vorgenommen. Um diese anzupassen, kann die Datei
`<tomcat-dir>\webapps\eventing-rest-api\WEB-INF\classes\wv\group-policies.json`
angepasst werden. Diese Datei ist beispielsweise wie folgt aufgebaut:

```json
{
    "adminGroupNames": [
        "admins",
        "sensorweb-admins"
    ],
    "editorGroupNames": [
        "publishers",
        "sensorweb-users"
    ],
    "restrictedSeriesIds": [
    ]
}

```

In dem JSON-Array `adminGroupNames` werden die Gruppennamen eingetragen, die
administrativen Zugang zum System haben (Anlegen von Regeln und Abonnements).

Ist ein Nutzer nicht in einer der Administratogruppen, kann er sich am System
anmelden, hat jedoch keine Schreibrechte.

## Neustart

Nach Anpassung der Konfiguration muss der Anwendungskontext (oder der gesamte
  Tomcat) neu gestartet werden. Dies kann beispielsweise über den Tomcat-Manager
  durchgeführt werden.

# API Kurzdokumentation

tbd
