package  modelbuilding.models.AGV

import modelbuilding.core._
import modelbuilding.core.simulation.Simulator

class SimulateAgv extends Simulator {

  val in1 = "in1"
  val in3 = "in3"
  val out = "out"
  val v1l = "v1l"
  val v2l = "v2l"
  val v3l = "v3l"
  val v4l = "v4l"
  val v5l = "v5l"
  val v1p = "v1p"
  val v2p = "v2p"
  val v3p = "v3p"
  val v4p = "v4p"
  val v5p = "v5p"
  val w1_11 = "w1_11"
  val w1_12 = "w1_12"
  val w1_2 = "w1_2"
  val w1_3 = "w1_3"
  val w1_4 = "w1_4"
  val w1_5 = "w1_5"
  val w2 = "w2"
  val w3 = "w3"

  override val initState: StateMap =
    StateMap(
      in1 -> true,
      in3 -> false,
      out -> false,
      v1l -> false,
      v2l -> false,
      v3l -> false,
      v4l -> false,
      v5l -> false,
      v1p -> "i1",
      v2p -> "w3i",
      v3p -> "z2",
      v4p -> "w1i2",
      v5p -> "o",
      w1_11 -> true,
      w1_12 -> true,
      w1_2 -> false,
      w1_3 -> false,
      w1_4 -> false,
      w1_5 -> false,
      w2 -> "w21",
      w3 -> "w31"
    )

  override val goalStates: Option[Set[StateMap]] = None

  override val guards: Map[Command,Predicate] = Map(
    c1 -> AND( EQ(v1p, "i1"), EQ(v1l, true)),
    c2 -> AND( EQ(v1p, "w2i"), EQ(v1l, false)),
    c3 -> AND( EQ(v2p, "i3"), EQ(v2l, true)),
    c4 -> AND( EQ(v2p, "w3i"), EQ(v2l, false)),
    c5 -> AND( EQ(v3p, "w1i1"), EQ(v3l, false)),
    c6 -> AND( EQ(v3p, "w2o"), EQ(v3l, true)),
    c7 -> AND( EQ(v4p, "w1i2"), EQ(v4l, false)),
    c8 -> AND( EQ(v4p, "w3o"), EQ(v4l, true)),
    c9 -> AND( EQ(v5p, "w1o"), EQ(v5l, true)),
    c10 -> AND( EQ(v5p, "o"), EQ(v5l, false)),
    e10 -> AND(EQ(v1p, "i1"), EQ(v1l, false), EQ(in1, true)),
    e11 -> EQ(in1, false),
    e20 -> EQ(out, true),
    e21 -> AND(EQ(v5p, "o"), EQ(v5l, true), EQ(out, false)),
    e30 -> AND(EQ(v2p, "i3"), EQ(v2l, false), EQ(in3, true)),
    e31 -> EQ(in3, false),
    u10 -> AND(EQ(v1p, "z1"), EQ(v1l, true)),
    u11 -> AND(EQ(v1p, "z1"), EQ(v1l, false)),
    u20 -> AND(EQ(v2p, "z3"), EQ(v2l, false)),
    u21 -> EQ(v2p, "z32"),
    u22 -> AND(EQ(v2p, "z2"), EQ(v2l, false)),
    u23 -> EQ(v2p, "z21"),
    u24 -> AND(EQ(v2p, "z1"), EQ(v2l, false)),
    u25 -> AND(EQ(v2p, "z1"), EQ(v2l, true)),
    u26 -> EQ(v2p, "z12"),
    u27 -> AND(EQ(v2p, "z2"), EQ(v2l, true)),
    u28 -> EQ(v2p, "z23"),
    u29 -> AND(EQ(v2p, "z3"), EQ(v2l, true)),
    u30 -> AND(EQ(v3p, "z2"), EQ(v3l, false)),
    u31 -> AND(EQ(v3p, "z2"), EQ(v3l, true)),
    u40 -> AND(EQ(v4p, "z3"), EQ(v4l, false)),
    u41 -> EQ(v4p, "z34"),
    u42 -> AND(EQ(v4p, "z4"), EQ(v4l, false)),
    u43 -> AND(EQ(v4p, "z4"), EQ(v4l, true)),
    u44 -> EQ(v4p, "z43"),
    u45 -> AND(EQ(v4p, "z3"), EQ(v4l, true)),
    u50 -> EQ(v5p, "zO4"),
    u51 -> AND(EQ(v5p, "z4"), EQ(v5l, false)),
    u52 -> AND(EQ(v5p, "z4"), EQ(v5l, true)),
    u53 -> EQ(v5p, "z4O"),
    w11 -> AND(EQ(w1_11,true), EQ(w1_12,true)),
    w12 -> EQ(w1_2, true),
    w13 -> AND(EQ(v5p, "w1o"), EQ(v5l, false), EQ(w1_3, true)),
    w14 -> AND(EQ(v3p, "w1i1"), EQ(v3l, true), EQ(w1_4, true)),
    w15 -> AND(EQ(v4p, "w1i2"), EQ(v4l, true), EQ(w1_5, true)),
    w21 -> EQ(w2, "w21"),
    w22 -> AND(EQ(v3p, "w2o"), EQ(v3l, false), EQ(w2, "w22")),
    w23 -> AND(EQ(v1p, "w2i"), EQ(v1l, true), EQ(w2, "w23")),
    w24 -> EQ(w2, "w24"),
    w31 -> AND(EQ(v4p, "w3o"), EQ(v4l, false), EQ(w3, "w31")),
    w32 -> AND(EQ(v2p, "w3i"), EQ(v2l, true), EQ(w3, "w32")),
    w33 -> EQ(w3, "w33"),
    w34 -> EQ(w3, "w34"),
  )

  override val actions: Map[Command,List[Action]] = Map(
    c1 -> List(Assign(v1p, "z1")),
    c2 -> List(Assign(v1p, "z1")),
    c3 -> List(Assign(v2p, "z1")),
    c4 -> List(Assign(v2p, "z3")),
    c5 -> List(Assign(v3p, "z2")),
    c6 -> List(Assign(v3p, "z2")),
    c7 -> List(Assign(v4p, "z3")),
    c8 -> List(Assign(v4p, "z4")),
    c9 -> List(Assign(v5p, "z4")),
    c10 -> List(Assign(v5p, "zO4")),
    e10 -> List(Assign(v1l, true), Toggle(in1)),
    e11 -> List(Toggle(in1)),
    e20 -> List(Toggle(out)),
    e21 -> List(Assign(v5l, false), Toggle(out)),
    e30 -> List(Assign(v2l, true), Toggle(in3)),
    e31 -> List(Toggle(in3)),
    u10 -> List(Assign(v1p, "w2i")),
    u11 -> List(Assign(v1p, "i1")),
    u20 -> List(Assign(v2p, "z32")),
    u21 -> List(Assign(v2p, "z2")),
    u22 -> List(Assign(v2p, "z21")),
    u23 -> List(Assign(v2p, "z1")),
    u24 -> List(Assign(v2p, "i3")),
    u25 -> List(Assign(v2p, "z12")),
    u26 -> List(Assign(v2p, "z2")),
    u27 -> List(Assign(v2p, "z23")),
    u28 -> List(Assign(v2p, "z3")),
    u29 -> List(Assign(v2p, "w3i")),
    u30 -> List(Assign(v3p, "w2o")),
    u31 -> List(Assign(v3p, "w1i1")),
    u40 -> List(Assign(v4p, "z34")),
    u41 -> List(Assign(v4p, "z4")),
    u42 -> List(Assign(v4p, "w3o")),
    u43 -> List(Assign(v4p, "z43")),
    u44 -> List(Assign(v4p, "z3")),
    u45 -> List(Assign(v4p, "w1i2")),
    u50 -> List(Assign(v5p, "z4")),
    u51 -> List(Assign(v5p, "w1o")),
    u52 -> List(Assign(v5p, "z4O")),
    u53 -> List(Assign(v5p, "o")),
    w11 -> List(Assign(w1_2, true), Assign(w1_11, false), Assign(w1_12, false)),
    w12 -> List(Assign(w1_3, true), Assign(w1_2, false)),
    w13 -> List(Assign(v5l, true), Assign(w1_4, true), Assign(w1_5, true), Assign(w1_3, false)),
    w14 -> List(Assign(v3l, false), Assign(w1_11, true), Assign(w1_4, false)),
    w15 -> List(Assign(v4l, false), Assign(w1_12, true), Assign(w1_5, false)),
    w21 -> List(Assign(w2, "w22")),
    w22 -> List(Assign(v3l, true), Assign(w2, "w23")),
    w23 -> List(Assign(v1l, false), Assign(w2, "w24")),
    w24 -> List(Assign(w2, "w21")),
    w31 -> List(Assign(v4l, true), Assign(w3, "w32")),
    w32 -> List(Assign(v2l, false), Assign(w3, "w33")),
    w33 -> List(Assign(w3, "w34")),
    w34 -> List(Assign(w3, "w31")),
  )

}
