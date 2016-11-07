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

import akka.actor.{ Actor, Props }
import akka.util.Timeout
import de.khive.examples.elevator.ElevatorApplication
import de.khive.examples.elevator.model.consoleinterface._
import de.khive.examples.elevator.model.elevator._
import de.khive.examples.elevator.model.elevatordispatcher._
import org.slf4j.LoggerFactory

import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * Console User Interface Actor
 *
 * Takes user input, parses the given commands and tells it to [[ElevatorDispatcher]]
 *
 * Created by ceth on 09.11.16.
 */
class ConsoleInterface extends Actor {

  val log = LoggerFactory.getLogger(getClass)

  implicit val timeout = Timeout(5 seconds)

  @SuppressWarnings(Array("org.brianmckenna.wartremover.warts.Any"))
  def receive: Receive = {
    case EnableConsoleInput => userInput()
    case t @ _ => {
      log.info(s"Unprocessable command: ${t}")
      ()
    }
  }

  def userInput(): Unit = {
    printHelp()
    for (ln <- io.Source.stdin.getLines.takeWhile(!_.equals("exit"))) {
      log.info(s"Command given: ${ln}")
      try {
        if (parseCommand(ln)) {
          Console.println("Command accepted!")
        } else {
          Console.println(s"Error with given command: ${ln}")
        }
      } catch {
        case e: Exception => log.error(e.getMessage, e)
      }
    }

    Console.println("Application shutdown (takes about 5 seconds) ... ")
    val result = context.system.terminate()
    Await.result(result, Timeout(5 seconds).duration)

    Console.println("Exiting... bye :-)")
    System.exit(0)
  }

  def parseCommand(cmd: String): Boolean = {
    val cmdParts: List[String] = cmd.split(" ").toList
    try {
      cmdParts match {
        case "call" :: rest => {
          if (rest.nonEmpty && rest.headOption.nonEmpty) {
            ElevatorApplication.elevatorDispatcher ! CallElevatorButtonPressed(rest(0).toString.toInt, getMotion(rest(1).toString))
            true
          } else {
            false
          }
        }
        case "move" :: rest => {
          if (rest.nonEmpty && rest.size == 2) {
            ElevatorApplication.elevatorDispatcher ! MoveToFloorButtonPressed(rest(0).toString.toInt, rest(1).toString.toInt)
            true
          } else {
            false
          }
        }
        case "help" :: rest => {
          printHelp()
          true
        }
        case _ => {
          Console.println(s"Unrecognized command: ${cmd}")
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

  def printHelp(): Unit = {
    Console.println(
      """Enter command:
        | Possible Motions:
        | -> up
        | -> down
        | Possible Commands:
        | -> call <yourFloorNumber> <motion> (call an elevator to your floor)
        | -> move <elevatorId> <targetFloorNumber> (request to move you to a target floor)
        | -> help (print this peace of text :-)
        | -> exit (quit elevator control!)
      """
    )
  }

}

object ConsoleInterface {
  val props = Props[ConsoleInterface]
}