Scala Examples - Reactive Elevator Control
==========================================

Purpose
-------
A Scala Example Project to show Akka and Akka Streams in action.

The Elevator

Properties
----------
- Floor Count
- Elevator Count

Run the Application
===================

```
Usage: example-elevator [options]

  -f, --floorCount <value>
                           floorCount is an integer property
  -e, --elevatorCount <value>
                           elevatorCount is an integer property
```

Starting the application with "java -jar ..." and passing configuration params on commandline:

```
&> java -jar example-elevator-<VERSION>.jar de.khive.examples.elevator.ElevatorApplication -f <floor_count> -e <elevator_count>
```

Use Case: Run application from commandline as an unix service
