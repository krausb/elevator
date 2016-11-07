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

package de.khive.examples.elevator

import com.typesafe.config.ConfigFactory

/**
 * Elevator Application Configuration Provider
 */
object ElevatorApplicationConfig {

  /**
   * Create [[ElevatorApplicationConfig]] from internal application.conf file.
   *
   * @return Option[ElevatorApplicationConfig]
   */
  def fromConfiguration(): ElevatorApplicationConfig = {
    val config = ConfigFactory.load()
    ElevatorApplicationConfig(config.getInt("elevator.floorCount"), config.getInt("elevator.elevatorCount"))
  }

  /**
   * Parse command line args into [[ElevatorApplicationConfig]]
   *
   * @return Option[ElevatorApplicationConfig]
   */
  def fromArgs(args: Array[String]): ElevatorApplicationConfig = {
    val parser = new scopt.OptionParser[ElevatorApplicationConfig]("example-elevator") {
      head("example-elevator", "1.0")

      opt[Int]('f', "floorCount").required().action((x, c) =>
        c.copy(floorCount = x)).text("floorCount is an integer property")
      opt[Int]('e', "elevatorCount").required().action((x, c) =>
        c.copy(elevatorCount = x)).text("elevatorCount is an integer property")
    }

    parser.parse(args, ElevatorApplicationConfig()) match {
      case Some(config) => config
      case None => {
        Console.println("Using application.conf file Configuration...")
        fromConfiguration()
      }
    }
  }

}

case class ElevatorApplicationConfig(floorCount: Int = -1, elevatorCount: Int = -1)