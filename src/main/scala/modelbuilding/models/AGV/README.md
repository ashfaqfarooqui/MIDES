# modelbuilding.models.AGV 
This is a model of the AGV system

The system depicts a manufacturing system with the following supply chain:

                                          -----------------
    Input 1   -->   Workstation 2   -->   |               |
                                          | Workstation 1 |   -->   Output
    Input 3   -->   Workstation 3   -->   |               |
                                          -----------------




## Events 

* Vehicle initiate partial sequence (going to or from a Station*) (CONTROLLABLE):
c1,c2,c3,c4,c5,c6,c7,c8,c9,c10
  
* Vehicle pass checkpoints along their sequences (UNCONTROLLABLE): u10,u11,u20,u21,u22,u23,u24,u25,u26,u27,u28,u29,u30,u31,u40,u41,u42,u43,u44,u45,u50,u51,u52,u53

* Operations within the Workstations (UNCONTROLLABLE):
w11,w12,w13,w14,w15,w21,w22,w23,w24,w31,w32,w33,w34

* Transaction of materials and products in the Input/Output stations (UNCONTROLLABLE)
e11,e10,e20,e21,e30,e31

\* Station refers to Input, Output or Workstation




## Support structures

Positions available to the vehicles:

	"z1", "z2", "z3", "z4",	// The crossings (mutex zones)

	"z12", "z21",           // Transition between zones. 
	"z23", "z32",           // (`zXzY` means transitions from zone `X` to zone `Y`
	"z34", "z43",           //

	"z4O", "zO4",           // There is a significant distance from z4 to the output.

	"i1",                   // Input 1
	"i3"                    // Input 3
	"o"                     // Output

	"w1i1",                 // Input 1 of Workstation 1
	"w1i2",                 // Input 2 of Workstation 1
	"w1o", 	                // Output of Workstation 1

	"w2i",                  // Input of Workstation 2
	"w2o",                  // Output of Workstation 2

	"w3i",                  // Input of Workstation 3
	"w3o",                  // Output of Workstation 3




## Variables

### Input/Output

	in1: Boolean
	in3: Boolean
	out: Boolean

### Vehicles 

Carrying item:

    v1l, v2l, v3l, v4l, v5l: Boolean

Positions:

	v1p: Domain("i1", "i1", "z1", "w2i", "w2i")
	v2p: Domain("i3", "i3", "z1", "z12", "z2", "z23", "z3", "z32", "z21", "w3i", "w3i")
	v3p: Domain("w2o", "w2o", "z2", "w1i1", "w1i1")
	v4p: Domain("w3o", "w3o", "z3", "z34", "z4", "z43", "w1i2", "w1i2")
	v5p: Domain("w1o", "w1o", "z4", "z4O", "zOz4", "o", "o")

### Workstations
* `wXY` means that Workstation `X` is ready for process `Y`
* Variables `wX_Y` means that Workstation `X` is ready for process `Y`
* Variables `wX_YZ` means that Workstation `X` is ready for process `Y` WHEN all similar variables are true


	w1_11: Boolean, w1_12: Boolean 		// Indicates that workstation 1 process 1 requires two inputs
	w1_2: Boolean
	w1_3: Boolean
	w1_4: Boolean
	w1_5: Boolean
	
	w2: Domain("w21", "w22", "w23", "w24")
	w3: Domain("w31", "w32", "w33", "w34")




## Modules

* Input1
  * Variables: `input1`, Events: `e10, e11`

* Input3
  * Variables: `input3`, Events: `e20, e21`

* Output
  * Variables: `output`, Events: `e20, e21`

* AGV1
  * Variables: `v1`, Events: `e10, w23, c1, c2, u10, u12`
* AGV2
  * Variables: `v2`, Events: `e20, w32, c3, c4, u20, u21, u22, u23, u24, u25, u26, u27, u28, u29` 
* AGV3
  * Variables: `v3`, Events: `w14, w22, c5, c6, u30, u31`
* AGV4
  * Variables: `v4`, Events: `w15, w31, c7, c8, u40, u41, u42, u43, u44, u45` 
* AGV5
  * Variables: `v5`, Events: `e20, w13, c8, c9, u50, u51, u52, u53`

* Workstation 1
  * Variables: `w1`, Events: `w11, w12, w13, w14, w15`
* Workstation 2
  * Variables: `w2`, Events: `w21, w22, w23, w24`
* Workstation 2
  * Variables: `w3`, Events: `w31, w32, w33, w34`


## Disclaimer
We were not the one to decide the unintuitive labeling of modules and events...
