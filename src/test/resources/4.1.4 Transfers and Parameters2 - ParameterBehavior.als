//*****************************************************************
// Module: 		Parameter Behaviors
// Written by:		Jeremy Doerr
// Purpose: 		Provides an example of using Occurrence to model calling behaviors
//				in which parameters are passed. 
//*****************************************************************
module ParameterBehavior
open Transfer[Occurrence] as o
open utilities/types/relation as r
abstract sig Occurrence {}

sig Real extends Occurrence{}

//*****************************************************************
/** 					A */
//*****************************************************************
sig A extends Occurrence{vout: set Real}

fact {all x: A | no x.inputs}
fact {all x: A | #x.vout=1}
fact {all x: A | x.vout in x.outputs}
fact {all x: A | x.outputs in x.vout}

//*****************************************************************
/** 					B1 */
//*****************************************************************
sig B1 extends Occurrence{vin, vout: set Real}

fact {all x: B1 | #x.vin=1}
fact {all x: B1 | #x.vout=1}
fact {all x: B1 | x.vin=x.vout}
fact {all x: B1 | x.vin in x.inputs}
fact {all x: B1 | x.inputs in x.vin}
fact {all x: B1 | x.vout in x.outputs}
fact {all x: B1 | x.outputs in x.vout}
//*****************************************************************
/** 					B2 */
//*****************************************************************
sig B2 extends Occurrence{vin, vout: set Real}

fact {all x: B2 | #x.vin=1}
fact {all x: B2 | #x.vout=1}
fact {all x: B2 | x.vin=x.vout}
fact {all x: B2 | x.vin in x.inputs}
fact {all x: B2 | x.inputs in x.vin}
fact {all x: B2 | x.vout in x.outputs}
fact {all x: B2 | x.outputs in x.vout}
//*****************************************************************
/** 					B */
//*****************************************************************
sig B extends Occurrence {
	vin, vout: set Real,
	b1: set B1,
	b2: set B2,
	disj transferBB1, transferB1B2, transferB2B: set Transfer
}

fact {all x: B | #x.vin=1}
fact {all x: B | #x.vout=1}
/** Constraints on the Transfer from input of B to input of B1 */
fact {all x: B | functionFiltered[sources, x.transferBB1, x]}
fact {all x: B | bijectionFiltered[targets, x.transferBB1, x.b1]}
fact {all x: B | subsettingItemRuleForSources[x.transferBB1]}
fact {all x: B | subsettingItemRuleForTargets[x.transferBB1]}
fact {all x: B | isBeforeTarget[x.transferBB1]}
/** Constraints on b1: B1 */
fact {all x: B | #x.b1 = 1}
/** Constraints on the Transfer from output of B1 to input of B2*/
fact {all x: B | bijectionFiltered[sources, x.transferB1B2, x.b1]}
fact {all x: B | bijectionFiltered[targets, x.transferB1B2, x.b2]}
fact {all x: B | subsettingItemRuleForSources[x.transferBB1]}
fact {all x: B | subsettingItemRuleForTargets[x.transferBB1]}
fact {all x: B | isAfterSource[x.transferB1B2]}
fact {all x: B | isBeforeTarget[x.transferB1B2]}
/** Constraints on b2: B2 */
//fact {all x: B | #x.b2=1}		// shouldn't be necessary with the TransferBefore between B1 and B2.
/** Constraints on the Transfer from output of B2 to output of B */
fact {all x: B | bijectionFiltered[sources, x.transferB2B, x.b2]}
fact {all x: B | functionFiltered[targets, x.transferB2B, x]}
fact {all x: B | subsettingItemRuleForSources[x.transferB2B]}
fact {all x: B | subsettingItemRuleForTargets[x.transferB2B]}
fact {all x: B | isAfterSource[x.transferB2B]}
/** Model closure */
fact {all x: B | x.b1 + x.b2 + x.transferBB1 + x.transferB1B2 + x.transferB2B in x.steps}
fact {all x: B | x.steps in x.b1 + x.b2 + x.transferBB1 + x.transferB1B2 + x.transferB2B}
fact {all x: B | x.vin in x.inputs}
fact {all x: B | x.inputs in x.vin}
fact {all x: B | x.vout in x.outputs}
fact {all x: B | x.outputs in x.vout}

//*****************************************************************
/** 					C */
//*****************************************************************
sig C extends Occurrence{vin: set Real}

fact {all x: C | #x.vin=1}
fact {all x: C | no x.outputs}
fact {all x: C | x.vin in x.inputs}
fact {all x: C | x.inputs in x.vin}
//*****************************************************************
/** 					ParameterBehavior */
//*****************************************************************
sig ParameterBehavior extends Occurrence{
	a: set A,
	b: set B,
	c: set C,
	disjoint transferAB, transferBC: set TransferBefore
}

fact {all x: ParameterBehavior | no x.inputs}
fact {all x: ParameterBehavior | no x.outputs}
/** Constraints on a: A */
fact {all x: ParameterBehavior | #x.a=1}
/** Constraints on the Transfer from output of A to input of B */
fact {all x: ParameterBehavior | bijectionFiltered[sources, x.transferAB, x.a]}
fact {all x: ParameterBehavior | bijectionFiltered[targets, x.transferAB, x.b]}
fact {all x: ParameterBehavior | subsettingItemRuleForSources[x.transferAB]}
fact {all x: ParameterBehavior | subsettingItemRuleForTargets[x.transferAB]}
fact {all x: ParameterBehavior | isAfterSource[x.transferAB]}
fact {all x: ParameterBehavior | isBeforeTarget[x.transferAB]}
/** Constraints on b:B */
//fact {all x: ParameterBehavior | #x.b=1}
/** Constraints on the Transfer from output of B to input of C*/
fact {all x: ParameterBehavior | bijectionFiltered[sources, x.transferBC, x.b]}
fact {all x: ParameterBehavior | bijectionFiltered[targets, x.transferBC, x.c]}
fact {all x: ParameterBehavior | subsettingItemRuleForSources[x.transferBC]}
fact {all x: ParameterBehavior | subsettingItemRuleForTargets[x.transferBC]}
fact {all x: ParameterBehavior | isAfterSource[x.transferBC]}
fact {all x: ParameterBehavior | isBeforeTarget[x.transferBC]}
fact {all x: ParameterBehavior | x.a + x.b + x.c + x.transferAB + x.transferBC in x.steps}
/** Constraints on c:C */
//fact {all x: ParameterBehavior | #x.c=1}
/** Model closure */
fact {all x: ParameterBehavior | x.steps in x.a + x.b + x.c + x.transferAB + x.transferBC}

//*****************************************************************
/** 			General Functions and Predicates */
//*****************************************************************
pred instancesDuringExample {all x:(A + B + B1 + B2 + C) | some steps.x}

//*****************************************************************
/** 				Checks and Runs */
//*****************************************************************
run parameterBehavior{instancesDuringExample and some ParameterBehavior} for 18
