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
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestActorRef, TestFSMRef, TestKit, TestProbe}
import akka.util.Timeout
import de.khive.examples.elevator.model.elevator._
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FlatSpecLike, MustMatchers}

import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Unit Test Suite for [[Elevator]]
  *
  * Created by ceth on 08.11.16.
  */
class ElevatorTestSuite extends TestKit(ActorSystem("ElevatorTestSuite"))  with ImplicitSender with FlatSpecLike
  with MustMatchers with BeforeAndAfter with BeforeAndAfterAll {

  implicit val timeout = Timeout(5 seconds)

  val fsm = TestFSMRef(new Elevator(id = 0, minLevel = 0, maxLevel = 10))

  val probe = TestProbe()

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "The elevator fsm" must "be initialized correctly" in {
    // test if elevator actor is correct typed
    val properlyTyped: TestActorRef[Elevator] = fsm

    // test initialization of elevator FSM
    assert(fsm.stateData.floor == 0)
    assert(fsm.stateData.motion == Idle)
    assert(fsm.stateName == Idle)
  }

  "The elevator fsm" should "receive an EnqueueFloor(...) message" in {
    // test an elevator call to floors and do the NextQueue steps
    val testMsg = EnqueueFloor(FloorRequest(3, probe.ref))
    fsm ! testMsg

    val upQueueFuture = fsm ? GetUpQueue
    val result = Await.result(upQueueFuture, Timeout(5 seconds).duration).asInstanceOf[mutable.Queue[FloorRequest]]

    assert(result == mutable.Queue[FloorRequest](FloorRequest(3, probe.ref)))
  }

  "The elevator fsm" should "move from initial level 0 to the requested and enqueued floor 3" in {
    // test an elevator call to floors and do the NextQueue steps
    val testMsg = EnqueueFloor(FloorRequest(3, probe.ref))
    fsm ! testMsg

    fsm ! NextQueue
    assert(fsm.stateData.floor == 1)
    assert(fsm.stateName == MovingUp)
    fsm ! NextQueue
    assert(fsm.stateData.floor == 2)
    assert(fsm.stateName == MovingUp)
    fsm ! NextQueue
    assert(fsm.stateData.floor == 3)
    assert(fsm.stateName == MovingUp)
    fsm ! NextQueue
    assert(fsm.stateData.floor == 3)
    assert(fsm.stateName == Idle)

    probe.expectMsg(BoardingNotification(0,3))
  }

}
