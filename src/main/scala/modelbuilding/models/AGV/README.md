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

	"z12", "z21",		// Transition between zones. 
	"z23", "z32",		// (`zXzY` means transitions from zone `X` to zone `Y`
	"z34", "z43",		//

	"z4O", "zO4",		// There is a significant distance from z4 to the output.

	"i11","i12", 		// At Input 1, ready to: (i) pickup material, (ii) leave station
	"i31","i32", 		// At Input 3, ready to: (i) pickup material, (ii) leave station
	"o1","o2", 		// At Output, ready to: (i) drop of product, (ii) leave station

	"w1i11","w1i12", 	// At input 1 of Workstation 1, ready to: (i) drop of material, (ii) leave station
	"w1i21","w1i22", 	// At input 2 of Workstation 1, ready to: (i) drop of material, (ii) leave station
	"w1o1","w1o2", 	// At output of Workstation 1, ready to: (i) pick up material, (ii) leave station

	"w2i1","w2i2", 	// At input of Workstation 2, ready to: (i) drop of material, (ii) leave station
	"w2o1","w2o2", 	// At output of Workstation 2, ready to: (i) pick up material, (ii) leave station

	"w3i1","w3i2", 	// At input of Workstation 3, ready to: (i) drop of material, (ii) leave station
	"w3o1","w3o2", 	// At output of Workstation 3, ready to: (i) pick up material, (ii) leave station




## Variables

### Input/Output

	in1: Boolean
	in3: Boolean
	out: Boolean

### Vehicles

	v1: Domain("i11", "i12", "z1", "w2i1", "w2i2")
	v2: Domain("i31", "i32", "z1", "z12", "z2", "z23", "z3", "z32", "z21", "w3i1", "w3i2")
	v3: Domain("w2o1", "w2o2", "z2", "w1i11", "w1i12")
	v4: Domain("w3o1", "w3o2", "z3", "z34", "z4", "z43", "w1i21", "w1i22")
	v5: Domain("w1o1", "w1o2", "z4", "z4O", "zOz4", "o1", "o2")

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
