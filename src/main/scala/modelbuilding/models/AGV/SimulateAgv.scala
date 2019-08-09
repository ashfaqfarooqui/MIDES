package  modelbuilding.models.AGV

import modelbuilding.core.modelInterfaces.Simulator
import modelbuilding.core._

class SimulateAgv extends Simulator {

  val in1 = "in1"
  val in3 = "in3"
  val out = "out"
  val v1 = "v1"
  val v2 = "v2"
  val v3 = "v3"
  val v4 = "v4"
  val v5 = "v5"
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
      v1 -> "i11",
      v2 -> "w3i2",
      v3 -> "z2",
      v4 -> "w1i22",
      v5 -> "o2",
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
    c1 -> EQ(v1, "i12"),
    c2 -> EQ(v1, "w2i2"),
    c3 -> EQ(v2, "i22"),
    c4 -> EQ(v2, "w3i2"),
    c5 -> EQ(v3, "w1i12"),
    c6 -> EQ(v3, "w2o2"),
    c7 -> EQ(v4, "w1i22"),
    c8 -> EQ(v4, "w3o2"),
    c9 -> EQ(v5, "w1o2"),
    c10 -> EQ(v5, "o2"),
    e10 -> AND(EQ(v1, "i11"), EQ(in1, true)),
    e11 -> EQ(in1, false),
    e20 -> AND(EQ(v5, "o1"), EQ(out, true)),
    e21 -> EQ(out, false),
    e30 -> AND(EQ(v2, "i21"), EQ(in3, true)),
    e31 -> EQ(in3, false),
    u10 -> EQ(v1, "z1"),
    u11 -> EQ(v1, "z1"),
    u20 -> EQ(v2, "z3"),
    u21 -> EQ(v2, "z32"),
    u22 -> EQ(v2, "z2"),
    u23 -> EQ(v2, "z21"),
    u24 -> EQ(v2, "z1"),
    u25 -> EQ(v2, "z1"),
    u26 -> EQ(v2, "z12"),
    u27 -> EQ(v2, "z2"),
    u28 -> EQ(v2, "z23"),
    u29 -> EQ(v2, "z3"),
    u30 -> EQ(v3, "z2"),
    u31 -> EQ(v3, "z2"),
    u40 -> EQ(v4, "z3"),
    u41 -> EQ(v4, "z34"),
    u42 -> EQ(v4, "z4"),
    u43 -> EQ(v4, "z4"),
    u44 -> EQ(v4, "z43"),
    u45 -> EQ(v4, "z3"),
    u50 -> EQ(v5, "zO4"),
    u51 -> EQ(v5, "z4"),
    u52 -> EQ(v5, "z4"),
    u53 -> EQ(v5, "z4O"),
    w11 -> AND(EQ(w1_11,true), EQ(w1_12,true)),
    w12 -> EQ(w1_2, true),
    w13 -> AND(EQ(v5, "w1o1"), EQ(w1_3, true)),
    w14 -> AND(EQ(v3, "w1i11"), EQ(w1_4, true)),
    w15 -> AND(EQ(v4, "w1i21"), EQ(w1_5, true)),
    w21 -> EQ(w2, "w21"),
    w22 -> AND(EQ(v3, "w2o1"), EQ(w2, "w22")),
    w23 -> AND(EQ(v1, "w2i1"), EQ(w2, "w23")),
    w24 -> EQ(w2, "w24"),
    w31 -> AND(EQ(v4, "w3o1"), EQ(w3, "w31")),
    w32 -> AND(EQ(v2, "w3i1"), EQ(w3, "w32")),
    w33 -> EQ(w3, "w33"),
    w34 -> EQ(w3, "w34"),
  )

  override val actions: Map[Command,List[Action]] = Map(
    c1 -> List(Assign(v1, "z1")),
    c2 -> List(Assign(v1, "z1")),
    c3 -> List(Assign(v2, "z1")),
    c4 -> List(Assign(v2, "z3")),
    c5 -> List(Assign(v3, "z2")),
    c6 -> List(Assign(v3, "z2")),
    c7 -> List(Assign(v4, "z3")),
    c8 -> List(Assign(v4, "z4")),
    c9 -> List(Assign(v5, "z4")),
    c10 -> List(Assign(v5, "z4")),
    e10 -> List(Assign(v1, "i12"), Toggle(in1)),
    e11 -> List(Toggle(in1)),
    e20 -> List(Assign(v5, "o2"), Toggle(out)),
    e21 -> List(Toggle(out)),
    e30 -> List(Assign(v2, "i22"), Toggle(in3)),
    e31 -> List(Toggle(in3)),
    u10 -> List(Assign(v1, "w1i1")),
    u11 -> List(Assign(v1, "i11")),
    u20 -> List(Assign(v2, "z32")),
    u21 -> List(Assign(v2, "z2")),
    u22 -> List(Assign(v2, "z21")),
    u23 -> List(Assign(v2, "z1")),
    u24 -> List(Assign(v2, "i21")),
    u25 -> List(Assign(v2, "z12")),
    u26 -> List(Assign(v2, "z2")),
    u27 -> List(Assign(v2, "z23")),
    u28 -> List(Assign(v2, "z3")),
    u29 -> List(Assign(v2, "w3i1")),
    u30 -> List(Assign(v3, "w2o1")),
    u31 -> List(Assign(v3, "w1i11")),
    u40 -> List(Assign(v4, "z34")),
    u41 -> List(Assign(v4, "z4")),
    u42 -> List(Assign(v4, "w3o1")),
    u43 -> List(Assign(v4, "z43")),
    u44 -> List(Assign(v4, "z3")),
    u45 -> List(Assign(v4, "w1i21")),
    u50 -> List(Assign(v5, "z4")),
    u51 -> List(Assign(v5, "w1o1")),
    u52 -> List(Assign(v5, "z4O")),
    u53 -> List(Assign(v5, "o1")),
    w11 -> List(Assign(w1_2, true), Assign(w1_11, false), Assign(w1_11, false)),
    w12 -> List(Assign(w1_3, true), Assign(w1_2, false)),
    w13 -> List(Assign(w1_4, true), Assign(w1_5, true), Assign(w1_3, false)),
    w14 -> List(Assign(w1_11, true), Assign(w1_4, false)),
    w15 -> List(Assign(w1_12, true), Assign(w1_5, false)),
    w21 -> List(Assign(w2, "w22")),
    w22 -> List(Assign(w2, "w23")),
    w23 -> List(Assign(w2, "w24")),
    w24 -> List(Assign(w2, "w21")),
    w31 -> List(Assign(w3, "w32")),
    w32 -> List(Assign(w3, "w33")),
    w33 -> List(Assign(w3, "w34")),
    w34 -> List(Assign(w3, "w31")),
  )

}
