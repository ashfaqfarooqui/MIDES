//
///*
// * Learning Automata for Supervisory Synthesis
// *  Copyright (C) 2019
// *
// *     This program is free software: you can redistribute it and/or modify
// *     it under the terms of the GNU General Public License as published by
// *     the Free Software Foundation, either version 3 of the License, or
// *     (at your option) any later version.
// *
// *     This program is distributed in the hope that it will be useful,
// *     but WITHOUT ANY WARRANTY; without even the implied warranty of
// *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// *     GNU General Public License for more details.
// *
// *     You should have received a copy of the GNU General Public License
// *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
// */
//
//package modelbuilding.algorithms.KV
//
//case class Element[+A](val value: A, count: Int = 1) {
//  override def toString = s"(${value},$count)"
//}
//
///**
//  * A tree definition, support for add, delete, search, size, height and pretty print
//  */
//sealed abstract class Tree[+A] {
//  val size: Int
//  val height: Int
//
//  def add[B >: A](b: B)(implicit ismember: B => Int): Tree[B]
//
//  def search[B >: A](b: B)(implicit ismember: B => Int): Boolean
//
//  protected def printLevel0(): Unit
//
//  protected def printOtherLevel(subLevel: Int): Unit
//
//  private[tree] def printGivenLevel(level: Int): Unit = level match {
//    case 0 => printLevel0()
//    case _ => printOtherLevel(level - 1)
//  }
//
//  def prettyPrint(): Unit = (0 to height + 1) foreach { h =>
//    printGivenLevel(h)
//    println()
//  }
//}
//
//object Tree {
//  def binaryTree[A](l: List[A])(implicit ordering: Ordering[A]) =
//    l.foldLeft(Empty: Tree[A])((b, a) => b.add(a))
//}
//
///**
//  * Act a stopper for the tree branches
//  */
//sealed case class Leaf[+A](v: A) extends Tree[A] {
//  val size   = 0
//  val height = -1
//
//  def add[A](b: A, d: A)(implicit ismember: A => Int) =
//    if (ismember(b + d) > 0)
//      Node(Element(b + d), Leaf(v), Leaf(b))
//    else
//      Node(Element(b + d), Leaf(b), Leaf(v))
//
//  def search[A >: Nothing](b: A)(implicit ismember: A => Int) = false
//
//  protected def printLevel0() = print("E")
//
//  protected def printOtherLevel(level: Int) = {
//    printGivenLevel(level)
//    print(" ")
//    printGivenLevel(level)
//  }
//}
//
//sealed case class Node[+A](el: Element[A], left: Tree[A], right: Tree[A])
//    extends Tree[A] {
//  val size   = 1 + left.size + right.size
//  val height = 1 + math.max(left.height, right.height)
//
//  /**
//    * If value already exists then increment count value
//    * If bigger then continue in the right subtree
//    * else in the left subtree
//    */
//  def add[B >: A](b: B)(implicit ismember: B => Int) = {
//    import ordering._
//    if (b == el.value) Node(Element(el.value, el.count + 1), left, right)
//    else if (b > el.value) Node(el, left, right.add(b))
//    else Node(el, left.add(b), right)
//  }
//
//  /**
//    * Found if same value otherwise
//    * if bigger, search in right subtree
//    * else search in left subtree
//    */
//  def search[B >: A](b: B)(implicit ismember: B => Int) = {
//    import ordering._
//    if (b == el.value) true
//    else if (b < el.value) left.search(b)
//    else right.search(b)
//  }
//
//  /**
//    * Pop the maximum element from the right subtree
//    * and return the max value and the new subtree
//    */
//  def popMaximum: (Element[A], Tree[A]) = right match {
//    case Empty => (el, left)
//    case nRight: Node[A] =>
//      val (max, t) = nRight.popMaximum
//      (max, Node(el, left, t))
//  }
//
//  protected def printLevel0() = print(el)
//
//  protected def printOtherLevel(level: Int) = {
//    left.printGivenLevel(level)
//    print(" ")
//    right.printGivenLevel(level)
//  }
//}
