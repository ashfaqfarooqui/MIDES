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

	enum(
		"z1", "z2", "z3", "z4",	// The crossings (mutex zones)

		"z12", "z21",		// Transition between zones. 
		"z23", "z32",		// (`zXzY` means transitions from zone `X` to zone `Y`
		"z34", "z43",		//

		"z4zO", "zOz4",		// There is a significant distance from z4 to the output.

		"i11","i12", 		// At Input 1, ready to: (i) pickup material, (ii) leave station
		"i31","i32", 		// At Input 3, ready to: (i) pickup material, (ii) leave station
		"o1","o2", 		// At Output, ready to: (i) drop of product, (ii) leave station

		"w1in11","w1in12", 	// At input 1 of Workstation 1, ready to: (i) drop of material, (ii) leave station
		"w1in21","w1in22", 	// At input 2 of Workstation 1, ready to: (i) drop of material, (ii) leave station
		"w1out1","w1out2", 	// At output of Workstation 1, ready to: (i) pick up material, (ii) leave station

		"w2in1","w2in2", 	// At input of Workstation 2, ready to: (i) drop of material, (ii) leave station
		"w2out1","w2out2", 	// At output of Workstation 2, ready to: (i) pick up material, (ii) leave station

		"w3in1","w3in2", 	// At input of Workstation 3, ready to: (i) drop of material, (ii) leave station
		"w3out1","w3out2", 	// At output of Workstation 3, ready to: (i) pick up material, (ii) leave station
	)




## Variables

### Input/Output

	input1: Boolean
	input3: Boolean
	output: Boolean

### Vehicles

	v1: Domain("i11", "i12", "z1", "w2in1", "w2in2")
	v2: Domain("i31", "i32", "z1", "z1z2", "z2", "z2z3", "z3", "z3z2", "z2z1", "w3in1", "w3in2")
	v3: Domain("w2out1", "w2out2", "z2", "w1in11", "w1in12")
	v4: Domain("w3out1", "w3out2", "z3", "z3z4", "z4", "z4z3", "w1in21", "w1in22")
	v5: Domain("w1out1", "w1out2", "z4", "z4zO", "zOz4", "o1", "o2")

### Workstations
* `wXin`/`wXout` means that Workstation `X` is ready for input/output of product.
* `wXinY`/`wXpY` means that Workstation `X` is ready for input/process `Y`


	w1: Domain("w1in1", "w1in2", "w1p1", "w1p2", "w1out")
	w2: Domain("w2in", "w2p1", "w2p2","w2out")
	w3: Domain("w3in", "w3p1", "w3p2","w3out")




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


## Disclaimer
We were not the one to decide the unintuitive labeling of modules and events...
