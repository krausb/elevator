/*
 * This file is part of example-elevator (further: this software).
 *
 * Copyright (C) 2016  Bastian Kraus
 *
 * This software is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version)
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.khive.examples.elevator.services

import akka.actor.{ Actor, ActorRef, Props }
import akka.pattern.ask
import akka.util.Timeout
import de.khive.examples.elevator.ElevatorApplicationConfig
import de.khive.examples.elevator.model.elevator.{ ElevatorNotFoundException, GetConfig, _ }
import de.khive.examples.elevator.model.elevatordispatcher._
import de.khive.examples.elevator.model.timestepper.{ DoStep, StartSteppingAutomation, StopSteppingAutomation }
import org.slf4j.LoggerFactory

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Random

/**
 * The [[Elevator]] Dispatcher is responsable for delegating service requests
 * to the registered elevators.
 *
 * Created by ceth on 09.11.16.
 */
class ElevatorDispatcher(config: ElevatorApplicationConfig) extends Actor with ElevatorControlSystem {

  var log = LoggerFactory.getLogger(getClass)

  implicit val timeout = Timeout(5 seconds)

  // initialize requested amount of elevators
  val elevators = for {
    eId <- 0 until config.elevatorCount
  } yield {
    val elevator = context.actorOf(Props(new Elevator(eId, 0, config.floorCount)))
    elevator ! Initialize(0)
    elevator
  }

  val timeStepper = context.system.actorOf(Props(new TimeStepperService(self)))

  override def receive: Receive = {
    case c @ CallElevator(sourceFloor, motion) => {
      log.info(s"Received ${c} ...")
      enqueueElevatorCall(sourceFloor, motion)
    }
    case m @ MoveToFloor(elevatorId, targetFloor) => {
      log.info(s"Received ${m} ...")
      getElevatorActorById(elevatorId) match {
        case Some(elevator) => elevator ! EnqueueFloor(FloorRequest(targetFloor, self))
        case _ => throw new ElevatorNotFoundException(s"Elevator with given id ${elevatorId} not found.")
      }
    }
    case b @ BoardingNotification(elevatorId, floor) => {
      log.info(s"Received ${b} ...")
      log.info(s"(BING) Boarding available for elevator ${elevatorId} on floor ${floor}")
    }
    case GetStatus => sender ! status()
    case DoStep(slots) => for (s <- 0 until slots) elevators.foreach(e => e ! NextQueue)
    case t @ (StopSteppingAutomation | StartSteppingAutomation) => timeStepper ! t
  }

  /**
   * Enqueue an elevator call for given floor and motion state
   *
   * @param floor
   * @param motion
   */
  def enqueueElevatorCall(floor: Int, motion: MotionState): Unit = {
    val availableElevators = elevators.filter(getMotionSelector(floor, motion))
    val targetElevator = if (availableElevators.nonEmpty) Random.shuffle(availableElevators).head else Random.shuffle(elevators).head
    log.info(s"Selected elevator to send to ${floor} with motion ${motion}: ${targetElevator}")
    targetElevator ! EnqueueFloor(FloorRequest(floor, self))
  }

  /**
   * Helper: delivers a filter function strategy used by [[ElevatorDispatcher.enqueueElevatorCall()]]
   *
   * @param floor
   * @param motion
   * @param timeout
   * @return
   */
  private def getMotionSelector(floor: Int, motion: MotionState)(implicit timeout: Timeout): ActorRef => Boolean =
    motion match {
      case _ =>
        (e) => {
          requestConfig(e) match {
            case Some(config) => {
              if (config.currentState.motion.eq(Idle) ||
                (config.currentState.motion.eq(MovingUp) && config.currentState.floor < floor) ||
                (config.currentState.motion.eq(MovingDown) && config.currentState.floor > floor)) {
                true
              } else {
                false
              }
            }
            case _ => false
          }
        }
    }

  /**
   * Helper for requesting the elevator config of given [[Elevator]]s [[ActorRef]]
   *
   * @param ref
   * @param timeout
   * @return Option[ElevatorConfig]
   */
  private def requestConfig(ref: ActorRef)(implicit timeout: Timeout): Option[ElevatorConfig] = {
    val rFuture = ref ? GetConfig
    try {
      Option(Await.result(rFuture, Timeout(5 seconds).duration).asInstanceOf[ElevatorConfig])
    } catch {
      case e: Exception => None
    }
  }

  /**
   * Request an [[Elevator]] by given elevatorId. Returns an [[Option]] containing the [[ActorRef]]
   * or an empty [[Option]] if no elevator with given ID is found.
   *
   * @param elevatorId
   * @return Option[ActorRef]
   */
  def getElevatorActorById(elevatorId: Int): Option[ActorRef] = {
    val rest = elevators.filter(e => {
      requestConfig(e) match {
        case Some(config) => if (config == elevatorId) true else false
        case _ => false
      }
    })
    if (rest.nonEmpty) {
      Option(rest(0))
    } else {
      None
    }
  }

  override def status(): Seq[ElevatorConfig] = elevators.flatMap(e => requestConfig(e))

  override def update(elevatorId: Int, sourceFloor: Int, targetFloor: Int): Unit = {
    update(elevatorId, sourceFloor)
    update(elevatorId, targetFloor)
  }

  override def update(elevatorId: Int, targetFloor: Int): Unit = {
    getElevatorActorById(elevatorId) match {
      case Some(elevatorRef) => elevatorRef ! EnqueueFloor(FloorRequest(targetFloor, self))
      case _ => throw new ElevatorNotFoundException(s"No elevator with ID ${elevatorId} found.")
    }
  }

  override def pickup(floor: Int, motion: MotionState): Unit = enqueueElevatorCall(floor, motion)

  override def step(slots: Int = 1): Unit = for (i <- 0 until slots) timeStepper ! DoStep

  override def stopStepping(): Unit = timeStepper ! StopSteppingAutomation

  override def startStepping(): Unit = timeStepper ! StartSteppingAutomation

}

object ElevatorDispatcher {
  val props = Props[ElevatorDispatcher]
}

/**
 * Elevator Control System interface
 *
 * Created by ceth on 08.11.16.
 */
trait ElevatorControlSystem {

  /**
   * Get the status of all available [[Elevator]]s
   *
   * @return
   */
  def status(): Seq[ElevatorConfig]

  /**
   * Move an [[Elevator]] selected given elevatorId from sourceFloor to the targetFloor
   *
   * @param elevatorId
   * @param sourceFloor
   * @param targetFloor
   * @throws ElevatorNotFoundException
   */
  def update(elevatorId: Int, sourceFloor: Int, targetFloor: Int): Unit

  /**
   * Send an [[Elevator]] given by elevatorId to targetFloor.
   *
   * @param elevatorId
   * @param targetFloor
   */
  def update(elevatorId: Int, targetFloor: Int): Unit

  /**
   * Request an [[Elevator]] to pick the passenger up from the given floor in direction of given
   * motion.
   *
   * @param floor
   * @param motion
   */
  def pickup(floor: Int, motion: MotionState): Unit

  /**
   * Advice internal the [[TimeStepperService]] to do a tick into the next timeslot.
   */
  def step(slots: Int = 1): Unit

  /**
   * Freeze the internal [[TimeStepperService]] automation
   */
  def stopStepping(): Unit

  /**
   * Start the internal [[TimeStepperService]] automation
   */
  def startStepping(): Unit

}
