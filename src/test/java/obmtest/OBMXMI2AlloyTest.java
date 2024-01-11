package obmtest;

import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import edu.gatech.gtri.obm.translator.alloy.AlloyUtils;
import edu.gatech.gtri.obm.translator.alloy.fromxmi.OBMXMI2Alloy;
import edu.mit.csail.sdg.ast.Expr;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.parser.CompModule;
import edu.umd.omgutil.UMLModelErrorException;

/*
 * Testing set up ===============
 * 
 * als files
 * 
 * from:Box\NIST OBM Translator\Alloy Models\obm-alloy-code_2023-09-25. zip\obm\*
 * 
 * to:obm-alloy-code_2023-09-25\obm
 * 
 * xmi files
 * 
 * from:Box\NIST OBM Translator\NIST UML-SysML OBM Models\obmsmttrans_2023-09-25.
 * zip\obmsmttrans\samples\OBMModel.xmi Box\NIST OBM Translator\NIST UML-SysML OBM
 * Models\obmsmttrans_2023-09-25. zip\obmsmttrans\samples\OBM.xmi
 * 
 * to:obm-alloy-code_2023-09-25\obm
 */

class OBMXMI2AlloyTest {

  @ParameterizedTest

  // order of fields matter
  @CsvSource({
      // model name is different, to fact {all x: AtomicBehavior | no x.steps}
      "4.1.5 Multiple Execution Steps2 - Multiple Object Flow Alt_mw.als,Model::Basic::MultipleObjectFlowAlt",
      // Fails
      "4.1.5 Multiple Execution Steps2 - Multiple Object Flow_mw.als, Model::Basic::MultipleObjectFlow",
      // Fails

      // fact {all x: AtomicBehavior | no y: Transfer | y in x.steps} to fact {all x: AtomicBehavior
      // | no x.steps}
      "4.1.5 Multiple Execution Steps - Multiple Control Flow_mw.als,Model::Basic::MultipleControlFlow",

      // WIP
      "4.2.2 FoodService Object Flow - IFSingleFoodService - OFFoodService_mw.als,Model::Realistic::IFFoodService",
      // modify function & inversefunction and bijectionFiltered
      "4.1.1 Control Nodes1 - SimpleSequence_mw.als, Model::Basic::SimpleSequence",
      "4.1.1 Control Nodes2 - Fork.als, Model::Basic::Fork",
      "4.1.1 Control Nodes3 - Join.als, Model::Basic::Join",
      // bijectionFiltered vs. functionFiltered and inverseFunctionFiltered
      "4.1.1 Control Nodes4 - Decision_mw.als, Model::Basic::Decision",
      // bijectionFiltered vs. functionFiltered and inverseFunctionFiltered
      "4.1.1 Control Nodes5 - Merge_mw.als, Model::Basic::Merge",
      "4.1.1 Control Nodes6 - AllControl.als, Model::Basic::AllControl",
      "4.1.2 LoopsExamples.als, Model::Basic::Loop",

      // module name CallingBehaviors vs. ComposedBehavior2
      // sig name ComposedBehavior vs. ComposedBehavior2
      // NestedBehavior p4, p5 to p1, p2 - change in model?
      // add fact {all x: NestedBehavior | #(x.p2) = 1}
      // add fact {all x: ComposedBehavior2 | #(x.p2) = 1}
      // add fact {all x: ComposedBehavior2 | #(x.p3) = 1}
      "4.1.3 CallingBehaviors_mw.als, Model::Basic::ComposedBehavior2",
      // add fact {all x: TransferProduct | no y: Transfer | y in x.steps}
      "4.1.4 Transfers and Parameters1 - TransferProduct_mw.als, Model::Basic::TransferProduct",
      // many difference see the file
      "4.1.4 Transfers and Parameters2 - ParameterBehavior_mw.als,Model::Basic::ParameterBehavior",

      // // 4.1.6
      // fact {all x: AtomicBehavior | no y: Transfer | y in x.steps} to fact {all x: AtomicBehavior
      // | no x.steps}
      "4.1.6 Unsatisfiable - Asymmetry_mw.als, Model::Basic::UnsatisfiableAsymmetry",
      // not available from jeremy
      "4.1.6 UnsatisfiableTransitivity.als, Model::Basic::UnsatisfiableTransitivity",
      "4.1.6 UnsatisfiableMultiplicity.als, Model::Basic::UnsatisfiableMultiplicity",
      "4.1.6 UnsatisfiableComposition1.als, Model::Basic::UnsatisfiableComposition1",
      "4.1.6 UnsatisfiableComposition2.als, Model::Basic::UnsatisfiableComposition2",

      "4.2.1 FoodService Control Flow - FoodService.als, Model::Realistic::FoodService",
      "4.2.1 FoodService Control Flow - SingleFoodService.als,Model::Realistic::SingleFoodService",
      "4.2.1 FoodService Control Flow - BuffetService.als, Model::Realistic::BuffetService",
      "4.2.1 FoodService Control Flow - ChurchSupperService.als, Model::Realistic::ChurchSupper",
      "4.2.1 FoodService Control Flow - FastFoodService.als, Model::Realistic::FastFoodService",
      "4.2.1 FoodService Control Flow - UsatisfiableFoodService.als,Model::Realistic::UnsatisfiableService",})


  /**
   * create an alloy file from a class named sysMLClassQualifiedName from Obm xmi file using Alloy
   * API. The created alloy file is imported using Alloy API again to find AllReachableFacts and
   * AllReachableUserDefinedSigs. Also, the manually created alloy file (manualFileName) is imported
   * using Alloy API to find its AllReachableFacts and AllReachableUserDefinedSigs. Then, the Sigs
   * and Expressions(Reachable facts) of manually created and generated by translator are compared.
   * 
   * @param manualFileName
   * @param sysMLClassQualifiedName
   * @throws FileNotFoundException
   * @throws UMLModelErrorException
   */
  void compare(String manualFileName, String sysMLClassQualifiedName)
      throws FileNotFoundException, UMLModelErrorException {

    System.out.println("Manually created alloy file = " + manualFileName);
    System.out.println("Comparing QualifiedName for a class = " + sysMLClassQualifiedName);

    // ========== Create Alloy model from OBM XMI file & write as a file ==========

    String ombmodel_dir = "src/test/resources";
    String output_and_testfiles_dir = "src/test/resources";
    File xmiFile = new File(ombmodel_dir, "OBMModel.xmi");

    // setting any errors to be in error file
    PrintStream o = new PrintStream(new File(output_and_testfiles_dir, "error.txt"));
    System.setErr(o);

    File apiFile = new File(output_and_testfiles_dir, manualFileName + "_Generated-"
        + sysMLClassQualifiedName.replaceAll("::", "_") /* alloyModule.getModuleName() */ + ".als");


    OBMXMI2Alloy test = new OBMXMI2Alloy(output_and_testfiles_dir);
    if (!test.createAlloyFile(xmiFile, sysMLClassQualifiedName, apiFile)) {
      fail("failed to create generated file: " + apiFile.getName());
    }


    // creating comparator
    ExpressionComparator ec = new ExpressionComparator();


    ////////////////////// Set up (Importing Modules) /////////////////////////////////////////
    // API
    CompModule apiModule = AlloyUtils.importAlloyModule(apiFile);
    // TEST
    File testFile = new File(output_and_testfiles_dir, manualFileName);
    System.out.println("testFile: " + testFile.exists() + "? " + testFile.getAbsolutePath());
    CompModule testModule = AlloyUtils.importAlloyModule(testFile);


    //////////////////////// Comparing Reachable Facts ////////////////////////////////
    // API
    Expr api_reachableFacts = apiModule.getAllReachableFacts();// test.getOverallFacts();
    System.out.println(api_reachableFacts);
    // TEST
    Expr test_reachableFacts = testModule.getAllReachableFacts();
    System.out.println(test_reachableFacts);
    // Compare
    assertTrue(ec.compareTwoExpressions(api_reachableFacts, test_reachableFacts));

    ///////////////////////// Comparing Sigs ////////////////////
    // API
    List<Sig> api_reachableDefinedSigs = apiModule.getAllReachableUserDefinedSigs();
    Map<String, Sig> api_SigByName = new HashMap<>();// test.getAllReachableUserDefinedSigs();
    for (Sig sig : api_reachableDefinedSigs) {
      api_SigByName.put(sig.label, sig);
    }
    // TEST
    List<Sig> test_reachableDefinedSigs = testModule.getAllReachableUserDefinedSigs();
    Map<String, Sig> test_SigByName = new HashMap<>();
    for (Sig sig : test_reachableDefinedSigs) {
      test_SigByName.put(sig.label, sig);
    }

    // Compare - Sig size
    assertTrue(api_SigByName.size() == test_SigByName.size());

    // Compare - Each sig
    for (String sigName : api_SigByName.keySet()) {
      Sig alloyFileSig = test_SigByName.get(sigName);
      Sig apiSig = api_SigByName.get(sigName);
      assertTrue(ec.compareTwoExpressions(alloyFileSig, apiSig));
    }
  }
}
