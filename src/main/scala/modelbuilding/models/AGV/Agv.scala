package  modelbuilding.models.AGV

import modelbuilding.core.modeling._
import modelbuilding.core.{Alphabet, StateSet}

object Agv extends ModularModel with Specifications {

  override val name: String = "AGV"
  override val modules: Set[String] = Set("Input 1", "Input 3", "Output", "AGV1", "AGV2", "AGV3", "AGV4", "AGV5", "WS1", "WS2", "WS3")

  override val alphabet: Alphabet = Alphabet(c1,c2,c3,c4,c5,c6,c7,c8,c9,c10,e11,e10,e20,e21,e30,e31,u10,u11,u20,u21,u22,u23,u24,u25,u26,u27,u28,u29,u30,u31,u40,u41,u42,u43,u44,u45,u50,u51,u52,u53,w11,w12,w13,w14,w15,w21,w22,w23,w24,w31,w32,w33,w34)

  val stateString: String = "in1 in3 out v1l v2l v3l v4l v5l v1p v2p v3p v4p v5p w1_11 w1_12 w1_2 w1_3 w1_4 w1_5 w2 w3"
  override val states: StateSet = StateSet(stateString.split(" ").toSet)

  override def stateMapping: Map[String,StateSet] = Map(
    "Input 1" -> StateSet("in1", "v1l", "v1p"),
    "Input 3" -> StateSet("in3", "v2l", "v2p"),
    "Output" -> StateSet("out", "v5l", "v5p"),
    "AGV1" -> StateSet("v1l", "v1p", "in1", "w2"),
    "AGV2" -> StateSet("v2l", "v2p", "in3", "w3"),
    "AGV3" -> StateSet("v3l", "v3p", "w1_4", "w2"),
    "AGV4" -> StateSet("v4l", "v4p", "w1_5", "w3"),
    "AGV5" -> StateSet("v5l", "v5p", "out", "w1_3"),
    "WS1" -> StateSet("w1_11","w1_12","w1_2","w1_3","w1_4","w1_5", "v3l", "v3p", "v4l", "v4p", "v5l", "v5p"),
    "WS2" -> StateSet("w2", "v1l", "v1p", "v3l", "v3p"),
    "WS3" -> StateSet("w3", "v2l", "v2p", "v4l", "v4p")
  )

  override val eventMapping:Map[String,Alphabet] = Map(

    "Input 1" -> Alphabet(e10, e11),
    "Input 3" -> Alphabet(e30, e31),
    "Output" -> Alphabet(e20, e21),
    "AGV1" -> Alphabet(e10, w23, c1, c2, u10, u11),
    "AGV2" -> Alphabet(e30, w32, c3, c4, u20, u21, u22, u23, u24, u25, u26, u27, u28, u29),
    "AGV3" -> Alphabet(w14, w22, c5, c6, u30, u31),
    "AGV4" -> Alphabet(w15, w31, c7, c8, u40, u41, u42, u43, u44, u45),
    "AGV5" -> Alphabet(e21, w13, c9, c10, u50, u51, u52, u53),
    "WS1" -> Alphabet(w11, w12, w13, w14, w15),
    "WS2" -> Alphabet(w21, w22, w23, w24),
    "WS3" -> Alphabet(w31, w32, w33, w34)
  )

  override val simulation = new SulAgv()
  override val specFilePath: Option[String] = Some("SupremicaModels/AGV.wmod")
  addSpecsFromSupremica(specFilePath.get)


}
