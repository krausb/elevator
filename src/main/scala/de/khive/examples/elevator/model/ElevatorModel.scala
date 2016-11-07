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

  sealed trait ElevatorCommand

  case class Initialize(floor: Int) extends ElevatorCommand

  case class EnqueueFloor(request: FloorRequest) extends ElevatorCommand

  case object NextQueue extends ElevatorCommand

  case object GetConfig extends ElevatorCommand

  sealed trait MotionState

  case object Idle extends MotionState

  case object MovingUp extends MotionState

  case object MovingDown extends MotionState

  sealed trait ElevatorData

  case class ElevatorConfig(elevatorId: Int, currentState: CurrentState) extends ElevatorData

  case class CurrentState(floor: Int, motion: MotionState) extends ElevatorData

  case class FloorRequest(floor: Int, ref: ActorRef) extends ElevatorData

  case class BoardingNotification(elevatorId: Int, floor: Int) extends ElevatorData

  case class FloorRequestError(elevatorId: Int, msg: String) extends ElevatorData

}