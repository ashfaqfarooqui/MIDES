/*
 * Learning Automata for Supervisory Synthesis
 *  Copyright (C) 2019
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package modelbuilding.algorithms.KV

import scala.annotation.tailrec

trait DiscriminationTree[A] {

  def value: Option[A] = this match {
    case n: Node[A] => Some(n.v)
    case l: Leaf[A] => Some(l.v)
    //    case Empty      => None
  }

  def left: Option[DiscriminationTree[A]] = this match {
    case n: Node[A] => Some(n.l)
    case l: Leaf[A] => None
    //    case Empty      => None
  }

  def right: Option[DiscriminationTree[A]] = this match {
    case n: Node[A] => Some(n.r)
    case l: Leaf[A] => None
    //   case Empty      => None
  }

  //States
  def getLeafValues: Set[A] = {
    @tailrec
    def loop(a: List[DiscriminationTree[A]], acc: Set[A]): Set[A] = {
      a match {
        case Nil                => acc
        case (n: Node[A]) :: tl => loop(n.left.get :: n.right.get :: tl, acc)
        case (l: Leaf[A]) :: tl => loop(tl, acc + l.value.get)
        case _ :: tl            => loop(tl, acc)
      }
    }

    loop(List(this), Set.empty[A])
  }

  //Discriminators
  def getNodeValues: Set[A] = {

    @tailrec
    def loop(a: List[DiscriminationTree[A]], acc: Set[A]): Set[A] = {
      a match {
        case Nil => acc
        case (n: Node[A]) :: tl =>
          loop(n.left.get :: n.right.get :: tl, acc + n.value.get)
        case (l: Leaf[A]) :: tl => loop(tl, acc)
        case _ :: tl            => loop(tl, acc)
      }
    }

    loop(List(this), Set.empty[A])
  }

  /*
Like `foldRight` for lists, `fold` receives a "handler" for each of the data constructors of the type, and recursively
accumulates some value using these handlers. As with `foldRight`, `fold(t)(Leaf(_))(Branch(_,_)) == t`, and we can use
this function to implement just about any recursive function that would otherwise be defined by pattern matching.
   */
  def fold[A, B](t: DiscriminationTree[A])(f: A => B)(g: (B, B) => B): B = t match {
    case Leaf(a)       => f(a)
    case Node(v, l, r) => g(fold(l)(f)(g), fold(r)(f)(g))
  }

}

case class Node[A](v: A, l: DiscriminationTree[A], r: DiscriminationTree[A])
    extends DiscriminationTree[A]

case class Leaf[A](v: A) extends DiscriminationTree[A] {}

//case object Empty extends DiscriminationTree[Nothing]
