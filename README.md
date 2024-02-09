# Praktikum Gruppe 37

## Konfiguration

### Windows

#### Datenbank starten

`docker-compose.exe -f docker-compose.yml up -d`

#### Anwendung starten

`.\gradlew bootRun --args="CLIENT_ID=<CLIENT_ID>;CLIENT_SECRET=<CLIENT_SECRET>"`

### Linux

#### Datenbank starten

`docker-compose -f docker-compose.yml up -d`

#### Anwendung starten

`CLIENT_ID=<CLIENT_ID> CLIENT_SECRET=<CLIENT_SECRET> gradle bootRun`

#### Ohne Termimal

All dies lässt sich natürlich auch ohne Terminal machen. Man kann über die IDE die docker-compose.yml File starten und dann die ChickenApplication.

#### Praktikumswerte

Unsere Anwendung erlaubt die Einstellung der Werte (wie Zeiten, maximaler erlaubter Urlaub, usw.) für das Praktikum. 
Die Werte lassen sich alle in der application.properties festlegen.

#### Beschreibung mit arc42

Eine Beschreibung der wesentlichen Komponenten und Entscheidungen unserer Anwendung befindet sich in der arc42-template.adoc im arc42-Ordner.
