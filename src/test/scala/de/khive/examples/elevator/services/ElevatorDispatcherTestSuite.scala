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

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import akka.util.Timeout
import de.khive.examples.elevator.ElevatorApplicationConfig
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FlatSpecLike, MustMatchers}

import scala.concurrent.duration._


/**
  * Unit Testing Suite for [[ElevatorDispatcher]]
  *
  * Created by ceth on 08.11.16.
  */
class ElevatorDispatcherTestSuite extends TestKit(ActorSystem("ElevatorTestSuite"))  with ImplicitSender with FlatSpecLike
  with MustMatchers with BeforeAndAfter with BeforeAndAfterAll {

  val dispatcher = TestActorRef[ElevatorDispatcher](new ElevatorDispatcher(ElevatorApplicationConfig(10,1)))

  implicit val timeout = Timeout(5 seconds)

  val probe = TestProbe()

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "The ElevatorDispatcher" should "initialize properly" in {
    val properlyTyped: TestActorRef[ElevatorDispatcher] = dispatcher

    assert(dispatcher.underlyingActor.elevators.size == 1)
  }

}
