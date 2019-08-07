package modelbuilding.core.modelInterfaces

import modelbuilding.core.Alphabets

trait Model{
  val name:String
  val A:Alphabets
  val simulation:SUL
}
