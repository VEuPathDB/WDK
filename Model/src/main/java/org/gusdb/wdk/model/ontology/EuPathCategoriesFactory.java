package org.gusdb.wdk.model.ontology;

import java.util.Collections;
import java.util.List;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;

import org.gusdb.fgputil.functional.FunctionalInterfaces.Predicate;
import org.gusdb.fgputil.functional.TreeNode;
import org.gusdb.fgputil.functional.TreeNode.StructureMapper;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.question.CategoryQuestionRef;
import org.gusdb.wdk.model.question.SearchCategory;
import org.apache.log4j.Logger;

/**
 * A temporary class to adapt from the eupath categories ontology to the wdk categories objects. Will be
 * retired when the client no longer uses wdk categories
 * 
 * Exposes a couple of public methods used by the WDK to serve out categories
 * 
 * For each usedBy (website, webservice, datasets), make a per-recordclass root searchcategory.
 * 
 * For genes, add to that the category tree found in the ontology
 * 
 * For non-genes, add to that one additional category, with the display name of the record class
 * 
 * Individuals are added to the leaf categories as question refs
 * 
 * @author steve
 *
 */
public class EuPathCategoriesFactory {

  private static final Logger LOG = Logger.getLogger(EuPathCategoriesFactory.class);

  // maps to collect the categories we make
  private Map<String, SearchCategory> websiteRootCategories = new LinkedHashMap<String, SearchCategory>();
  private Map<String, SearchCategory> webserviceRootCategories = new LinkedHashMap<String, SearchCategory>();
  private Map<String, SearchCategory> datasetRootCategories = new LinkedHashMap<String, SearchCategory>();
  private Map<String, SearchCategory> websiteCategories = new LinkedHashMap<String, SearchCategory>();
  private Map<String, SearchCategory> webserviceCategories = new LinkedHashMap<String, SearchCategory>();
  private Map<String, SearchCategory> datasetCategories = new LinkedHashMap<String, SearchCategory>();
  WdkModel model;

  public EuPathCategoriesFactory(WdkModel model) throws WdkModelException {
    this.model = model;
    getCategories();
  }

  public Map<String, SearchCategory> getRootCategories(String usedBy) {
    if (usedBy.equals(SearchCategory.USED_BY_WEBSERVICE))
      return Collections.unmodifiableMap(webserviceRootCategories);
    if (usedBy.equals(SearchCategory.USED_BY_WEBSITE))
      return Collections.unmodifiableMap(websiteRootCategories);
    if (usedBy.equals(SearchCategory.USED_BY_DATASET))
      return Collections.unmodifiableMap(datasetRootCategories);
    return null;
  }

  public Map<String, SearchCategory> getCategories(String usedBy) {
    if (usedBy.equals(SearchCategory.USED_BY_WEBSERVICE))
      return Collections.unmodifiableMap(webserviceCategories);
    if (usedBy.equals(SearchCategory.USED_BY_WEBSITE))
      return Collections.unmodifiableMap(websiteCategories);
    if (usedBy.equals(SearchCategory.USED_BY_DATASET))
      return Collections.unmodifiableMap(datasetCategories);
    return null;
  }

  // scopes
  private final String INTERNAL = "internal";
  private final String MENU = "menu";
  private final String WEBSERVICE = "webservice";

  private String[][] mbioRecordClassInfo = {
      { "Samples", "SampleRecordClasses.MicrobiomeSampleRecordClass" }
  };

  private String[][] clinepiRecordClassInfo = {
      //old ... deprecated?
      { "Participants", "ParticipantRecordClasses.ParticipantRecordClass" },
      { "Dwellings", "DwellingRecordClasses.DwellingRecordClass" },
      { "Clinical Visits", "ClinicalVisitRecordClasses.ClinicalVisitRecordClass" },
      //PRISM
      { "Participants", "DS_0ad509829eParticipantRecordClasses.DS_0ad509829eParticipantRecordClass" },
      { "Households", "DS_0ad509829eHouseholdRecordClasses.DS_0ad509829eHouseholdRecordClass" },
      { "Observations", "DS_0ad509829eObservationRecordClasses.DS_0ad509829eObservationRecordClass" },
      { "CDC Light Traps", "DS_0ad509829eLightTrapRecordClasses.DS_0ad509829eLightTrapRecordClass" },
      //PRISM2
      { "Participants", "DS_51b40fe2e2ParticipantRecordClasses.DS_51b40fe2e2ParticipantRecordClass" },
      { "Households", "DS_51b40fe2e2HouseholdRecordClasses.DS_51b40fe2e2HouseholdRecordClass" },
      { "Observations", "DS_51b40fe2e2ObservationRecordClasses.DS_51b40fe2e2ObservationRecordClass" },
      { "CDC Light Traps", "DS_51b40fe2e2LightTrapRecordClasses.DS_51b40fe2e2LightTrapRecordClass" },
      //India longitudinal
      { "Participants", "DS_05ea525fd3ParticipantRecordClasses.DS_05ea525fd3ParticipantRecordClass" },
      { "Observations", "DS_05ea525fd3ObservationRecordClasses.DS_05ea525fd3ObservationRecordClass" },
      //      { "Households", "DS_05ea525fd3HouseholdRecordClasses.DS_05ea525fd3HouseholdRecordClass" },
      //India cross sectional
      { "Participants", "DS_a5c969d5faParticipantRecordClasses.DS_a5c969d5faParticipantRecordClass" },
      { "Observations", "DS_a5c969d5faObservationRecordClasses.DS_a5c969d5faObservationRecordClass" },
      //      { "Households", "DS_a5c969d5faHouseholdRecordClasses.DS_a5c969d5faHouseholdRecordClass" },
      //India Fever study
      { "Participants", "DS_4902d9b7ecParticipantRecordClasses.DS_4902d9b7ecParticipantRecordClass" },
      //GEMs 
      { "Participants", "DS_841a9f5259ParticipantRecordClasses.DS_841a9f5259ParticipantRecordClass" },
      //GEMs1a 
      { "Participants", "DS_2a6ace17a1ParticipantRecordClasses.DS_2a6ace17a1ParticipantRecordClass" },
      //Maled DCC phase2
      { "Participants", "DS_3dbf92dc05ParticipantRecordClasses.DS_3dbf92dc05ParticipantRecordClass" },
      { "Observations", "DS_3dbf92dc05ObservationRecordClasses.DS_3dbf92dc05ObservationRecordClass" },
      // south asia
      { "Participants", "DS_13c737a528ParticipantRecordClasses.DS_13c737a528ParticipantRecordClass" },
      { "Observations", "DS_13c737a528ObservationRecordClasses.DS_13c737a528ObservationRecordClass" },
      // amazonia peru
      { "Participants", "DS_897fe55e6fParticipantRecordClasses.DS_897fe55e6fParticipantRecordClass" },
      { "Observations", "DS_897fe55e6fObservationRecordClasses.DS_897fe55e6fObservationRecordClass" },
      //SCORE
      { "Participants", "DS_d6a1141fbfParticipantRecordClasses.DS_d6a1141fbfParticipantRecordClass" },
  };

  // record classes whose individuals all have both scope website and menu
  private String[][] otherRecordClassInfo = {
      { "RFLP Genotype Isolates", "RflpIsolateRecordClasses.RflpIsolateRecordClass" },
      { "Popset Isolate Sequences", "PopsetRecordClasses.PopsetRecordClass" },
      { "Genomic Sequences", "SequenceRecordClasses.SequenceRecordClass" },
      { "Genomic Segments", "DynSpanRecordClasses.DynSpanRecordClass" },
      { "SNPs", "SnpRecordClasses.SnpRecordClass" },
      { "SNPs (from Array)", "SnpChipRecordClasses.SnpChipRecordClass" },
      { "ESTs", "EstRecordClasses.EstRecordClass" },
      { "ORFs", "OrfRecordClasses.OrfRecordClass" },
      { "Metabolic Pathways", "PathwayRecordClasses.PathwayRecordClass" },
      { "Compounds", "CompoundRecordClasses.CompoundRecordClass" }
  };

  private void getCategories() throws WdkModelException {

    Ontology ontology = model.getOntology("Categories");

    // Gene questions for menus
    processPrimaryCategoryQuestions(ontology, scopes(MENU), "Genes",
        "TranscriptRecordClasses.TranscriptRecordClass", websiteCategories, websiteRootCategories);
    // sort using ontology's sorting order, if present, else alphabetical.
    for (SearchCategory category : websiteCategories.values()) {
      // set the display name in the ref, so it can be used for sorting. ignore questions not found in model
      for (CategoryQuestionRef ref : category.getQuestionRefs()) {
        try {
          ref.setQuestionDisplayName(model.getQuestion(ref.getQuestionFullName()).getDisplayName());
        }
        catch (WdkModelException e) {}
      }
      List<CategoryQuestionRef> questionRefs = new ArrayList<CategoryQuestionRef>(category.getQuestionRefs());
      Collections.sort(questionRefs);
      Map<String, CategoryQuestionRef> sortedMap = new LinkedHashMap<String, CategoryQuestionRef>();
      for (CategoryQuestionRef ref : questionRefs)
        sortedMap.put(ref.getQuestionFullName(), ref);
      category.setResolvedQuestionRefMap(sortedMap);
    }
    // Sample questions for webservice
    processPrimaryCategoryQuestions(ontology, scopes(WEBSERVICE), "Genes",
        "TranscriptRecordClasses.TranscriptRecordClass", webserviceCategories, webserviceRootCategories);

    // gene questions for datasets
    processPrimaryCategoryQuestions(ontology, scopes(INTERNAL), "Genes",
        "TranscriptRecordClasses.TransciptRecordClass", datasetCategories, datasetRootCategories);

    // non-gene questions
    String[][] infos;
    switch (model.getProjectId()) {
      case "MicrobiomeDB": infos = mbioRecordClassInfo;    break;
      case "ClinEpiDB":    infos = clinepiRecordClassInfo; break;
      case "Gates":        infos = clinepiRecordClassInfo; break;
      case "ICEMR":        infos = clinepiRecordClassInfo; break;
      default:             infos = otherRecordClassInfo;
    }

    for (String[] recordClassInfo : infos) {
      TreeNode<OntologyNode> prunedOntologyTree = findPrunedOntology(ontology, recordClassInfo[1], scopes(MENU, WEBSERVICE));
      if (prunedOntologyTree == null)
        continue;
      List<Map<String, SearchCategory>> mapList = new ArrayList<Map<String, SearchCategory>>();
      mapList.add(webserviceCategories);
      mapList.add(websiteCategories);
      SearchCategory almostRootCategory = prunedOntologyTree.mapStructure(
          new TreeNodeToSeachCategoryMapper(mapList, false));
      almostRootCategory.setDisplayName(recordClassInfo[0]);
      almostRootCategory.setName("almostRoot");
      SearchCategory rootCategory = new SearchCategory();
      rootCategory.setWdkModel(model); // do this before adding question refs
      rootCategory.setDisplayName(recordClassInfo[0]);
      rootCategory.setName(recordClassInfo[1]);
      rootCategory.addChild(almostRootCategory);
      websiteRootCategories.put(rootCategory.getName(), rootCategory);
      webserviceRootCategories.put(rootCategory.getName(), rootCategory);
    }
  }

  // convenience method to create an array from varargs
  private static String[] scopes(String... scopes) {
    return scopes;
  }

  private void processPrimaryCategoryQuestions(Ontology ontology, String[] scopes, String displayName,
      String name, Map<String, SearchCategory> categoriesMap, Map<String, SearchCategory> rootCategoriesMap) {

    TreeNode<OntologyNode> prunedOntologyTree = findPrunedOntology(ontology, name, scopes);
    if (prunedOntologyTree == null)
      return;

    // pass to mapper the categoriesMap to stuff the new categories into
    List<Map<String, SearchCategory>> mapList = new ArrayList<Map<String, SearchCategory>>();
    mapList.add(categoriesMap);

    // map the pruned ontology to a new root search category. the root category holds the record class (for
    // this scope)
    SearchCategory rootCategory = prunedOntologyTree.mapStructure(
        new TreeNodeToSeachCategoryMapper(mapList, true));
    rootCategory.setDisplayName(displayName);
    rootCategory.setName(name);
    rootCategoriesMap.put(rootCategory.getName(), rootCategory);
  }

  private TreeNode<OntologyNode> findPrunedOntology(Ontology ontology, String recordClassName,
      String scopes[]) {
    Predicate<OntologyNode> predicate = new IsSearchPredicate(recordClassName, scopes);
    return Ontology.getFilteredOntology(ontology, predicate, false);
  }

  private class TreeNodeToSeachCategoryMapper implements StructureMapper<OntologyNode, SearchCategory> {

    private List<Map<String, SearchCategory>> maps;
    boolean isGenes;

    TreeNodeToSeachCategoryMapper(List<Map<String, SearchCategory>> maps, boolean isGenes) {
      this.maps = maps;
      this.isGenes = isGenes;
    }

    @Override
    public SearchCategory map(OntologyNode nodeContents, List<SearchCategory> mappedChildren) {

      // make new search category
      SearchCategory category = new SearchCategory();
      category.setWdkModel(model); // do this before adding question refs

      // if 0 mapped children, we have an individual.
      // the SearchCategory tree is non-uniform. individuals are not nodes.
      // because we run bottom-up, we have to temporarily pretend it is.
      // so make fake SearchCategory to hold an individual. will fix when we build parent
      // steal the isFlattenInMenu flag to indicate this.
      if (mappedChildren.size() == 0) {
        category.setFlattenInMenu(true); // a hacked flag to indicate this is an individual
        category.setName(nodeContents.get("name").get(0)); // the question full name
        List<String> scope = nodeContents.get("scope");
        if (scope.contains(MENU) && scope.contains(WEBSERVICE))
          category.setUsedBy(null);
        else if (scope.contains(MENU))
          category.setUsedBy(SearchCategory.USED_BY_WEBSITE);
        else if (scope.contains(WEBSERVICE))
          category.setUsedBy(SearchCategory.USED_BY_WEBSERVICE);
        else if (scope.contains(INTERNAL))
          category.setUsedBy(SearchCategory.USED_BY_DATASET);
      }

      // handle category
      else {

        // set properties
        category.setDescription(
            nodeContents.containsKey("hasDefinition") ? nodeContents.get("hasDefinition").get(0) : null);
        String displayName = nodeContents.containsKey("EuPathDB alternative term")
            ? nodeContents.get("EuPathDB alternative term").get(0) : null;
        category.setDisplayName(displayName);
        category.setShortDisplayName(nodeContents.containsKey("shortDisplayName")
            ? nodeContents.get("shortDisplayName").get(0) : null);
        category.setName(nodeContents.containsKey("name") ? nodeContents.get("name").get(0) : displayName);

        // handle mapped children
        // if child an individual, stuff into new category as a question ref
        for (SearchCategory kid : mappedChildren) {
          if (kid.isFlattenInMenu()) { // an individual
            CategoryQuestionRef qr = new CategoryQuestionRef();
            qr.setText(kid.getName());
            qr.setUsedBy(kid.getUsedBy());
            category.addResolvedQuestionRef(qr);
          }

          // otherwise, add as child search category
          else {
            if (isGenes)
              category.addChild(kid);

            // for non-genes, move question refs up to new search category, because we want to end up with
            // only one level
            else
              for (CategoryQuestionRef qr : kid.getQuestionRefs())
                category.addResolvedQuestionRef(qr);
          }
        }
      }

      if (category.getName() != null) {
        category.setName(category.getName().replaceAll(",", "_").replaceAll(" ", "_"));
        for (Map<String, SearchCategory> map : maps)
          map.put(category.getName(), category);
      }
      return category;
    }
  }

  private class IsSearchPredicate implements Predicate<OntologyNode> {

    private String scopes[];
    private String recordClass;

    IsSearchPredicate(String recordClass, String scopes[]) {
      this.recordClass = recordClass;
      this.scopes = scopes;
    }

    @Override
    public boolean test(OntologyNode node) {
      boolean hasScope = false;
      for (String scope : scopes)
        if (node.containsKey("scope") && node.get("scope").contains(scope))
          hasScope = true;
      try {
        return node.containsKey("targetType") && node.get("targetType").contains("search") &&
            node.containsKey("recordClassName") && node.get("recordClassName").contains(recordClass) &&
            model.getQuestion(node.get("name").get(0)) != null && hasScope;
      }
      catch (WdkModelException e) {
        LOG.debug("Error attempting to resolve ontology node with model entity.");
        LOG.debug(e.getMessage());
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        LOG.debug(sw.toString());
        return false;
      }
    }
  }

}
