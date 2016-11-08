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
import scala.collection.immutable.Queue

package elevator {

  /**
   * Trait to define an [[de.khive.examples.elevator.services.Elevator]] command
   */
  sealed trait ElevatorCommand

  /**
   * Initialize an [[de.khive.examples.elevator.services.Elevator]] with floor
   *
   * @param floor
   */
  case class Initialize(floor: Int) extends ElevatorCommand

  /**
   * Enqueue a [[FloorRequest]]
   *
   * @param request
   */
  case class EnqueueFloor(request: FloorRequest) extends ElevatorCommand

  /**
   * Trigger a queue step
   */
  case object NextQueue extends ElevatorCommand

  /**
   * Ask for the [[ElevatorConfig]]
   */
  case object GetConfig extends ElevatorCommand

  /**
   * Ask for the upwards queue
   */
  case object GetUpQueue extends ElevatorCommand

  /**
   * Aks for the downwards queue
   */
  case object GetDownQueue extends ElevatorCommand

  /**
   * Trait for FSM motion state
   */
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
        case _ => throw new IllegalArgumentException(s"Illegal motion: ${motionString}")
      }
    }
  }

  /**
   * Elevator is in Idle state
   */
  case object Idle extends MotionState

  /**
   * Elevator is moving upwards
   */
  case object MovingUp extends MotionState

  /**
   * Elevator is moving downwards
   */
  case object MovingDown extends MotionState

  /**
   * Trait to define [[de.khive.examples.elevator.services.Elevator]] data
   */
  sealed trait ElevatorData

  /**
   * An [[de.khive.examples.elevator.services.Elevator]]s current configuration
   *
   * @param elevatorId
   * @param currentState
   * @param upQueue
   * @param downQueue
   */
  case class ElevatorConfig(elevatorId: Int, currentState: CurrentState, upQueue: Queue[FloorRequest], downQueue: Queue[FloorRequest]) extends ElevatorData

  /**
   * An [[de.khive.examples.elevator.services.Elevator]]s current location and motion state
   *
   * @param floor
   * @param motion
   */
  case class CurrentState(floor: Int, motion: MotionState) extends ElevatorData

  /**
   * A request for enqueuing to move the [[de.khive.examples.elevator.services.Elevator]] to a specific floor
   * and notifying te set [[ActorRef]] with a /[[BoardingNotification]].
   * @param floor
   * @param ref
   */
  case class FloorRequest(floor: Int, ref: ActorRef) extends ElevatorData

  /**
   * A notification about door is opening on a specific floor for a specific elevator sent to the [[ActorRef]]
   * in the enqueued [[FloorRequest]].
   *
   * @param elevatorId
   * @param floor
   */
  case class BoardingNotification(elevatorId: Int, floor: Int) extends ElevatorData

  /**
   * An error sent if a [[FloorRequest]] was unable to be enqueued.
   *
   * @param elevatorId
   * @param msg
   */
  case class FloorRequestError(elevatorId: Int, msg: String) extends ElevatorData

  /**
   * Exception thrown in cases of requesting missing elevators
   *
   * @param ex
   */
  class ElevatorNotFoundException private (ex: RuntimeException) extends RuntimeException(ex) {
    def this(message: String) = this(new RuntimeException(message))
    def this(message: String, throwable: Throwable) = this(new RuntimeException(message, throwable))
  }

  object ElevatorNotFoundException {
    def apply(message: String) = new ElevatorNotFoundException(message)
    def apply(message: String, throwable: Throwable) = new ElevatorNotFoundException(message, throwable)
  }

}