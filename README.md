Scala Examples - Reactive Elevator Control
==========================================

Purpose
-------
A Scala Example Project to show Akka and Akka Streams in action.

The system simulates a set of elevators managed by a dispatcher. You can control the system via CLI
by entering control commands and step the simulation forward automatically or manually.

Available CLI Commands
----------------------

```
Possible Motions:
-> up
-> down
Possible Commands:
-> status (get all the elevator states)
-> call <yourFloorNumber> <motion> (call an elevator to your floor)
-> move <elevatorId> <targetFloorNumber> (request to move you to a target floor)
-> startstep (start automated time stepping)
-> stopstep (stop automated time stepping)
-> step [<stepCount>] (do steps, stepCount optional)
-> help (print this peace of text :-)
-> exit (quit elevator control!)
```

Properties
----------
- Floor Count
- Elevator Count

Implementation approach
-----------------------

The are many different approaches for building an elevator control and also different 
development environments. Here are three common ways to go: 

a)  A fully single-threaded synchronous approach with Elevators implemented through the State and Observer Pattern
b)  A multithreaded system approach with each elevator as a thread in a random programming language
    supporting multithreading and synchronization mechanisms
b)  A reactive asynchronous system approach with each elevator as an actor implemented with Java and Vert.X (http://vertx.io/)
    Verticles or in Scala with Akka (http://akka.io/) and reactive Actors

The way i will go here is the reactive fancy Scala and Akka way. This could truly also be implemented exactly the same way
in Java but for the current showcase i will go the former Scala and Akka one.

Why reactive?
Each component of the system will be implemented as a so called Actor which can receive asynchronous command messages
on which the receiving Actor can react and trigger some processes doing subprocessing or algorithm stuff.

Reactive systems are build with the event loop / message loop or message dispatcher paradigma. Each event in form of
a message will be queued and sequentially processed by the message dispatcher in mostly one single thread. This approach
has many advantages:
- No I/O wait times
- Optimal cpu time usage because each message is processed sequentially and no wait times occure on the cpu
- Fully asynchronous programming: You don't have to wait for a response - so called "Click-and-Forget" principle
- There is no need for resource synchronization e.g. Locking Objects or Mutexes because every event is processed seperatly

Build the Application
=====================

The Application uses SBT 0.13.13 for management. To compile the project use the following command:

```
sbt clean compile
```

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

Run Unit Test Suites and generate code coverage reporting
=========================================================

Run tests and generate coverage data:
```
sbt clean coverage test
```

Generate the code coverage HTML report after running tests:
```
sbt coverageReport
```