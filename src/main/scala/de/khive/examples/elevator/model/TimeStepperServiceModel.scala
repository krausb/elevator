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

package timestepper {

  /**
   * Trait to define a [[de.khive.examples.elevator.services.TimeStepperService]] command
   */
  sealed trait TimeStepperServiceCommand

  /**
   * Case class to distribute a do step advice
   *
   * @param slots
   */
  case class DoStep(slots: Int = 1) extends TimeStepperServiceCommand

  /**
   * Advice the [[de.khive.examples.elevator.services.TimeStepperService]] to start automated
   * stepping.
   */
  case object StartSteppingAutomation extends TimeStepperServiceCommand

  /**
   * Advice the [[de.khive.examples.elevator.services.TimeStepperService]] to stop automated
   * stepping.
   */
  case object StopSteppingAutomation extends TimeStepperServiceCommand

}