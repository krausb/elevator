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
import de.khive.examples.elevator.model.consoleinterface._
import de.khive.examples.elevator.services.{ConsoleInterface, ElevatorDispatcher}
import org.slf4j.LoggerFactory

/**
 * Elevator Example Application
 *
 * This class is startable by 'java -jar ...'
 *
 * Created by ceth on 09.11.16.
 */
object ElevatorApplication extends App {

  private val log = LoggerFactory.getLogger(getClass)

  val config = ElevatorApplicationConfig.fromArgs(args)
  log.info(s"Using configuration: ${config}")
  val system = ActorSystem("example-elevator")

  lazy val elevatorDispatcher = system.actorOf(Props(new ElevatorDispatcher(config)))
  lazy val consoleInterface = system.actorOf(ConsoleInterface.props)
  consoleInterface ! EnableConsoleInput
}
