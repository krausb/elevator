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

package de.khive.examples.elevator.model

import akka.actor.ActorRef

package elevator {

  import scala.collection.immutable.Queue

  sealed trait ElevatorCommand

  case class Initialize(floor: Int) extends ElevatorCommand

  case class EnqueueFloor(request: FloorRequest) extends ElevatorCommand

  case object NextQueue extends ElevatorCommand

  case object GetConfig extends ElevatorCommand

  case object GetUpQueue extends ElevatorCommand

  case object GetDownQueue extends ElevatorCommand

  sealed trait MotionState

  /**
    * Motion state static methods
    */
  object MotionState {

    /**
      * Parse a given str and return a motion state
      *
      * @param motionString
      * @return
      */
    def fromString(motionString: String): MotionState = {
      motionString match {
        case str if motionString.toLowerCase().startsWith("up") => MovingUp
        case str if motionString.toLowerCase().startsWith("down") => MovingDown
        case _ => throw new IllegalArgumentException (s"Illegal motion: ${motionString}")
      }
    }
  }

  case object Idle extends MotionState

  case object MovingUp extends MotionState

  case object MovingDown extends MotionState

  sealed trait ElevatorData

  case class ElevatorConfig(elevatorId: Int, currentState: CurrentState, upQueue: Queue[FloorRequest], downQueue: Queue[FloorRequest]) extends ElevatorData

  case class CurrentState(floor: Int, motion: MotionState) extends ElevatorData

  case class FloorRequest(floor: Int, ref: ActorRef) extends ElevatorData

  case class BoardingNotification(elevatorId: Int, floor: Int) extends ElevatorData

  case class FloorRequestError(elevatorId: Int, msg: String) extends ElevatorData

  /**
    * Exception thrown in cases of requesting missing elevators
    *
    * @param ex
    */
  class ElevatorNotFoundException private(ex: RuntimeException) extends RuntimeException(ex) {
    def this(message:String) = this(new RuntimeException(message))
    def this(message:String, throwable: Throwable) = this(new RuntimeException(message, throwable))
  }

  object ElevatorNotFoundException {
    def apply(message:String) = new ElevatorNotFoundException(message)
    def apply(message:String, throwable: Throwable) = new ElevatorNotFoundException(message, throwable)
  }


}