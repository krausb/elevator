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

import akka.actor.{ActorSystem, Props}
import de.khive.examples.elevator.model.EnableConsoleInput
import de.khive.examples.elevator.services.{ConsoleInterface, ElevatorDispatcher}
import org.slf4j.LoggerFactory

/**
  * Created by ceth on 04.11.16.
  */
object ElevatorApplication extends App {

  private val log = LoggerFactory.getLogger(getClass)

  //val config = getConfig()
  val config = Option(ElevatorApplicationConfig(5,1))

  if(config.isEmpty) throw new IllegalStateException("Config is empty.")

  val system = ActorSystem("example-elevator")
  lazy val elevatorDispatcher = system.actorOf(Props(new ElevatorDispatcher(config.get)))
  lazy val consoleInterface = system.actorOf(ConsoleInterface.props)
  consoleInterface ! EnableConsoleInput

  /**
    * Helper: parse command line args into [[ElevatorApplicationConfig]]
    *
    * @return Option[Config]
    */
  private def getConfig(): Option[ElevatorApplicationConfig] = {
    val parser = new scopt.OptionParser[ElevatorApplicationConfig]("example-elevator") {
      head("example-elevator", "1.0")

      opt[Int]('f', "floorCount").required().action( (x, c) =>
        c.copy(floorCount = x) ).text("floorCount is an integer property")
      opt[Int]('e', "elevatorCount").required().action( (x, c) =>
        c.copy(elevatorCount = x) ).text("elevatorCount is an integer property")
    }

    parser.parse(args, ElevatorApplicationConfig()) match {
      case Some(config) => Option(config)
      case None => {
        None
      }
    }
  }

}
