package AGV

import modelbuilding.core.{Command, Controllable, Uncontrollable}

trait AGVDomain {
  override def toString: String = this match {
    case `c1`  => "c1"
    case `c2`  => "c2"
    case `c3`  => "c3"
    case `c4`  => "c4"
    case `c5`  => "c5"
    case `c6`  => "c6"
    case `c7`  => "c7"
    case `c8`  => "c8"
    case `c9`  => "c9"
    case `c10` => "c10"
    case `e10` => "e10"
    case `e11` => "e11"
    case `e20` => "e20"
    case `e21` => "e21"
    case `e30` => "e30"
    case `e31` => "e31"
    case `u10` => "u10"
    case `u11` => "u11"
    case `u20` => "u20"
    case `u21` => "u21"
    case `u22` => "u22"
    case `u23` => "u23"
    case `u24` => "u24"
    case `u25` => "u25"
    case `u26` => "u26"
    case `u27` => "u27"
    case `u28` => "u28"
    case `u29` => "u29"
    case `u30` => "u30"
    case `u31` => "u31"
    case `u40` => "u40"
    case `u41` => "u41"
    case `u42` => "u42"
    case `u43` => "u43"
    case `u44` => "u44"
    case `u45` => "u45"
    case `u50` => "u50"
    case `u51` => "u51"
    case `u52` => "u52"
    case `u53` => "u53"
    case `w11` => "w11"
    case `w12` => "w12"
    case `w13` => "w13"
    case `w14` => "w14"
    case `w15` => "w15"
    case `w21` => "w21"
    case `w22` => "w22"
    case `w23` => "w23"
    case `w24` => "w24"
    case `w31` => "w31"
    case `w32` => "w32"
    case `w33` => "w33"
    case `w34` => "w34"

  }
}

case object c1  extends Command with AGVDomain with Controllable
case object c2  extends Command with AGVDomain with Controllable
case object c3  extends Command with AGVDomain with Controllable
case object c4  extends Command with AGVDomain with Controllable
case object c5  extends Command with AGVDomain with Controllable
case object c6  extends Command with AGVDomain with Controllable
case object c7  extends Command with AGVDomain with Controllable
case object c8  extends Command with AGVDomain with Controllable
case object c9  extends Command with AGVDomain with Controllable
case object c10 extends Command with AGVDomain with Controllable
case object e10 extends Command with AGVDomain with Uncontrollable
case object e11 extends Command with AGVDomain with Uncontrollable
case object e20 extends Command with AGVDomain with Uncontrollable
case object e21 extends Command with AGVDomain with Uncontrollable
case object e30 extends Command with AGVDomain with Uncontrollable
case object e31 extends Command with AGVDomain with Uncontrollable
case object u10 extends Command with AGVDomain with Uncontrollable
case object u11 extends Command with AGVDomain with Uncontrollable
case object u20 extends Command with AGVDomain with Uncontrollable
case object u21 extends Command with AGVDomain with Uncontrollable
case object u22 extends Command with AGVDomain with Uncontrollable
case object u23 extends Command with AGVDomain with Uncontrollable
case object u24 extends Command with AGVDomain with Uncontrollable
case object u25 extends Command with AGVDomain with Uncontrollable
case object u26 extends Command with AGVDomain with Uncontrollable
case object u27 extends Command with AGVDomain with Uncontrollable
case object u28 extends Command with AGVDomain with Uncontrollable
case object u29 extends Command with AGVDomain with Uncontrollable
case object u30 extends Command with AGVDomain with Uncontrollable
case object u31 extends Command with AGVDomain with Uncontrollable
case object u40 extends Command with AGVDomain with Uncontrollable
case object u41 extends Command with AGVDomain with Uncontrollable
case object u42 extends Command with AGVDomain with Uncontrollable
case object u43 extends Command with AGVDomain with Uncontrollable
case object u44 extends Command with AGVDomain with Uncontrollable
case object u45 extends Command with AGVDomain with Uncontrollable
case object u50 extends Command with AGVDomain with Uncontrollable
case object u51 extends Command with AGVDomain with Uncontrollable
case object u52 extends Command with AGVDomain with Uncontrollable
case object u53 extends Command with AGVDomain with Uncontrollable
case object w11 extends Command with AGVDomain with Uncontrollable
case object w12 extends Command with AGVDomain with Uncontrollable
case object w13 extends Command with AGVDomain with Uncontrollable
case object w14 extends Command with AGVDomain with Uncontrollable
case object w15 extends Command with AGVDomain with Uncontrollable
case object w21 extends Command with AGVDomain with Uncontrollable
case object w22 extends Command with AGVDomain with Uncontrollable
case object w23 extends Command with AGVDomain with Uncontrollable
case object w24 extends Command with AGVDomain with Uncontrollable
case object w31 extends Command with AGVDomain with Uncontrollable
case object w32 extends Command with AGVDomain with Uncontrollable
case object w33 extends Command with AGVDomain with Uncontrollable
case object w34 extends Command with AGVDomain with Uncontrollable
