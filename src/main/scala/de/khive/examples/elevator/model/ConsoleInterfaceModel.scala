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

package consoleinterface {

  /**
   * Trait for defining a [[de.khive.examples.elevator.services.ConsoleInterface]] actor command
   */
  sealed trait ConsoleInterfaceCommand

  /**
   * Initialize the [[de.khive.examples.elevator.services.ConsoleInterface]] with elevatorDispatcher
   *
   * @param elevatorDispatcher
   */
  case class InitializeConsoleInterface(elevatorDispatcher: ActorRef) extends ConsoleInterfaceCommand

  /**
   * Advice the [[de.khive.examples.elevator.services.ConsoleInterface]] [[akka.actor.Actor]] to start to consume user input from STDIN
   */
  case object EnableConsoleInput extends ConsoleInterfaceCommand

}