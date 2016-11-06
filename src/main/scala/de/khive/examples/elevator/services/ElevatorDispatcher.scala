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

import akka.actor.{Actor, Props}
import de.khive.examples.elevator.Config
import org.slf4j.LoggerFactory

import scala.util.Random

/**
  * The [[Elevator]] Dispatcher is responsable for delegating service requests
  * to the registered elevators.
  *
  * Created by ceth on 05.11.16.
  */
class ElevatorDispatcher(config: Config) extends Actor {

  var log = LoggerFactory.getLogger(getClass)

  // initialize requested amount of elevators
  val elevators = for {
    eId <- 0 until config.elevatorCount
  } yield context.actorOf(Props(new Elevator(eId, 0, config.floorCount))).asInstanceOf[Elevator]

  elevators.foreach(e => e.self ! Initialize(0))

  override def receive: Receive = {
    case CallElevatorButtonPressed(sourceFloor, motion) =>
      enqueueElevatorCall(sourceFloor, motion)
    case MoveToFloorButtonPressed(elevatorId, targetFloor) =>
      elevators.filter(e => e.getId == elevatorId)(0).self ! EnqueueFloor(FloorRequest(targetFloor, self))
    case BoardingNotification(elevatorId,floor) =>
      log.info(s"(BING) Boarding available for elevator ${elevatorId} on floor ${floor}")
  }

  def enqueueElevatorCall(floor: Int, motion: MotionState): Unit = {
    val availableElevators = elevators.filter(getMotionSelector(floor, motion))
    val targetElevator = if(availableElevators.nonEmpty) Random.shuffle(availableElevators).head.self else Random.shuffle(elevators).head.self
    targetElevator ! EnqueueFloor(FloorRequest(floor, self))
  }

  def getMotionSelector(floor: Int, motion: MotionState): Elevator => Boolean =
    motion match {
      case MovingUp =>
        (e) => e.stateData.motion.eq(Idle) ||
          (e.stateData.motion.eq(MovingUp) && e.stateData.floor < floor) ||
          (e.stateData.motion.eq(MovingDown))
      case MovingDown =>
        (e) => e.stateData.motion.eq(Idle) ||
          (e.stateData.motion.eq(MovingDown) && e.stateData.floor > floor) ||
          (e.stateData.motion.eq(MovingUp))
      case _ => throw new IllegalArgumentException(s"Motion selector for motion '${motion.getClass.getSimpleName}' not implemented.")
    }

}

object ElevatorDispatcher {
  val props = Props[ElevatorDispatcher]
}

sealed trait ElevatorDispatcherCommand
case class CallElevatorButtonPressed(sourceFloor: Int, motion: MotionState) extends ElevatorDispatcherCommand
case class MoveToFloorButtonPressed(elevatorId: Int, targetFloor: Int) extends ElevatorDispatcherCommand