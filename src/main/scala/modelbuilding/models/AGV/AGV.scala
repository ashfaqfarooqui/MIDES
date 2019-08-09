package models.AGV

import modelbuilding.core.Alphabet
import modelbuilding.core.modelInterfaces.ModularModel
import modelbuilding.core.modelInterfaces.ModularModel.Module
import modelbuilding.models.MachineBuffer.SULMachineBuffer
import org.supremica.automata.{Automata, Automaton}

object AGV extends ModularModel {

  override val name: String = "AGV"
  override val alphabet: Alphabet = Alphabet(c1,c2,c3,c4,c5,c6,c7,c8,c9,c10,e11,e10,e20,e21,e30,e31,u10,u11,u20,u21,u22,u23,u24,u25,u26,u27,u28,u29,u30,u31,u40,u41,u42,u43,u44,u45,u50,u51,u52,u53,w11,w12,w13,w14,w15,w21,w22,w23,w24,w31,w32,w33,w34)

  override val eventMapping:Map[Module,Alphabet] = Map(
    "WS1a" -> Alphabet(w11,w12,w13,w14),
    "WS1b" -> Alphabet(w11,w12,w13,w15),
    "Seq5" -> Alphabet(c10,c9,e20,u50,u51,u52,u53,w13),
    "Seq4" -> Alphabet(c7,c8,u40,u41,u42,u43,u44,u45,w15,w31),
    "Seq3" -> Alphabet(c5,c6,u30,u31,w14,w22),
    "Seq2" -> Alphabet(c3,c4,e30,u20,u21,u22,u23,u24,u25,u26,u27,u28,u29,w32),
    "Seq1" -> Alphabet(c1,c2,e10,u10,u11,w23),
    "Input1" -> Alphabet(e10,e11),
    "WS3" -> Alphabet(w31,w32,w33,w34),
    "WS2" -> Alphabet(w21,w22,w23,w24),
    "Input3" -> Alphabet(e30,e31),
    "Output" -> Alphabet(e20,e21)
  )

  override val simulation = new SULAGV()

  val suprFile: String = "./supremicaFiles/AGV.wmod"

}
