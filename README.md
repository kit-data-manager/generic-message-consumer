# generic-message-consumer

Generic consumer for AMQP messages sent by services, e.g. repository or authentication.

# Start the Consumer

java -cp generic-message-consumer-<VERSION>.jar -Dloader.path=<path_to_your_additional_jars> org.springframework.boot.loader.PropertiesLauncher