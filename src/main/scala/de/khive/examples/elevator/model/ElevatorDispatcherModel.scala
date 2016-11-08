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

import de.khive.examples.elevator.model.elevator._

package elevatordispatcher {

  /**
   * Trait for defining an [[de.khive.examples.elevator.services.ElevatorDispatcher]] command
   */
  sealed trait ElevatorDispatcherCommand

  /**
   * Ask for a [[Seq]] of [[ElevatorConfig]] of all available [[de.khive.examples.elevator.services.Elevator]]
   */
  case object GetStatus extends ElevatorDispatcherCommand

  /**
   * Call the elevator to a specific floor considering the motion state.
   *
   * @param sourceFloor
   * @param motion
   */
  case class CallElevator(sourceFloor: Int, motion: MotionState) extends ElevatorDispatcherCommand

  /**
   * Command to send to elevator dispatcher for moving [[de.khive.examples.elevator.services.Elevator]] with given elevatorId to
   * a targetFloor.
   *
   * @param elevatorId
   * @param targetFloor
   */
  case class MoveToFloor(elevatorId: Int, targetFloor: Int) extends ElevatorDispatcherCommand

}