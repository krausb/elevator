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
import de.khive.examples.elevator.ElevatorApplication
import de.khive.examples.elevator.model.consoleinterface._
import de.khive.examples.elevator.model.elevator._
import de.khive.examples.elevator.model.elevatordispatcher._
import org.slf4j.LoggerFactory

/**
  *
  *
  * Maintainer: BFFT Gesellschaft fuer Fahrzeugtechnik mbH
  * Created by: <a href="mailto:bastian.kraus@bfft.de">Bastian Kraus</a>
  * Created on: 07.11.2016
  */
class ConsoleInterface extends Actor {

  val log = LoggerFactory.getLogger(getClass)

  def receive: Receive = {
    case EnableConsoleInput => userInput()
    case t @ _ => log.info(s"Unprocessable command: ${t}")
  }

  def userInput(): Unit = {
    Console.println(
      """Enter command:
        | Possible Motions:
        | -> up
        | -> down
        | Possible Commands:
        | -> call <yourFloorNumber> <motion> (call an elevator to your floor)
        | -> move <elevatorId> <targetFloorNumber> (request to move you to a target floor)
        | -> exit (quit elevator control!)
      """)
    for(ln <- io.Source.stdin.getLines.takeWhile(!_.equals("exit"))) {
      log.info(s"Command given: ${ln}")
      if(parseCommand(ln)) {
        Console.println("Command accepted!")
      } else {
        Console.println(s"Error with given command: ${ln}")
      }
    }

  }

  def parseCommand(cmd: String): Boolean = {
    val cmdParts: List[String] = cmd.split(" ").toList
    try {
      cmdParts match {
        case "call" :: rest => {
          ElevatorApplication.elevatorDispatcher ! CallElevatorButtonPressed(rest.head.toString.toInt, getMotion(rest(1).toString))
          true
        }
        case "move" :: rest => {
          ElevatorApplication.elevatorDispatcher ! MoveToFloorButtonPressed(rest.head.toString.toInt, rest(1).toString.toInt)
          true
        }
        case "quit" :: rest => {
          log.info("Exiting... bye :-)")
          System.exit(0)
          true
        }
        case _ => {
          log.info(s"Unrecognized command: ${cmd}")
          false
        }
      }
    } catch {
      case e: Exception => {
        log.error(s"An error on parsing given command occured: ${e.getMessage}", e)
        false
      }
    }
  }

  def getMotion(motionString: String): MotionState = {
    motionString match {
      case str if motionString == "up" => MovingUp
      case str if motionString == "down" => MovingDown
      case _ => throw new IllegalArgumentException(s"Illegal motion: ${motionString}")
    }
  }

}

object ConsoleInterface {
  val props = Props[ConsoleInterface]
}