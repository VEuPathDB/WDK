package org.gusdb.wdk.model.ontology;

import java.util.Collections;
import java.util.List;
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
 * A temporary class to adapt from the eupath categories ontology to the wdk categories objects.
 * Will be retired when the client no longer uses wdk categories
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
  
  @SuppressWarnings("unused")
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
    if (usedBy.equals(SearchCategory.USED_BY_WEBSERVICE)) return Collections.unmodifiableMap(webserviceRootCategories);
    if (usedBy.equals(SearchCategory.USED_BY_WEBSITE)) return Collections.unmodifiableMap(websiteRootCategories);  
    if (usedBy.equals(SearchCategory.USED_BY_DATASET)) return Collections.unmodifiableMap(datasetRootCategories);  
    return null;
  }
  
  public Map<String, SearchCategory> getCategories(String usedBy) {
    if (usedBy.equals(SearchCategory.USED_BY_WEBSERVICE)) return Collections.unmodifiableMap(webserviceCategories);
    if (usedBy.equals(SearchCategory.USED_BY_WEBSITE)) return Collections.unmodifiableMap(websiteCategories);  
    if (usedBy.equals(SearchCategory.USED_BY_DATASET)) return Collections.unmodifiableMap(datasetCategories);  
    return null;
  }
  
  
  
  // scopes
  private final String INTERNAL = "internal";
  private final String MENU = "menu";
  private final String WEBSERVICE = "webservice";
  
  // record classes whose individuals all have both scope website and menu
  private String[][] otherRecordClassInfo = {{"Isolates", "IsolateRecordClasses.IsolateRecordClass"}, {"Genomic Sequences", "SequenceRecordClasses.SequenceRecordClass"}, {"Genomic Segments", "DynSpanRecordClasses.DynSpanRecordClass"}, {"SNPs", "SnpRecordClasses.SnpRecordClass"}, {"SNPs (from Chips)", "SnpChipRecordClasses.SnpChipRecordClass"}, {"ESTs", "EstRecordClasses.EstRecordClass"}, {"ORFs", "OrfRecordClasses.OrfRecordClass"}, {"Metabolic Pathways", "PathwayRecordClasses.PathwayRecordClass"}, {"Compounds", "CompoundRecordClasses.CompoundRecordClass"} };
  
  private  void getCategories() throws WdkModelException {

    Ontology ontology = model.getOntology("Categories");

    // Gene questions for menus
    String[] scopes2 = { MENU};
    processGeneQuestions(ontology, scopes2, websiteCategories, websiteRootCategories);

    // gene questions for webservice
    String[] scopes3 = { WEBSERVICE };
    processGeneQuestions(ontology, scopes3, webserviceCategories, webserviceRootCategories);

    // gene questions for datasets
    String[] scopes4 = { INTERNAL };
    processGeneQuestions(ontology, scopes4, datasetCategories, datasetRootCategories);

    // non-gene questions
    String[] scopes1 = { MENU, WEBSERVICE };
    for (String[] recordClassInfo : otherRecordClassInfo) {
      TreeNode<OntologyNode> prunedOntologyTree = findPrunedOntology(ontology, recordClassInfo[1], scopes1);
      List<Map<String, SearchCategory>> mapList =  new ArrayList<Map<String, SearchCategory>>();
      mapList.add(webserviceCategories);
      mapList.add(websiteCategories);
      SearchCategory almostRootCategory = prunedOntologyTree.mapStructure(new TreeNodeToSeachCategoryMapper(mapList, false));
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
  
  private void processGeneQuestions(Ontology ontology, String[] scopes, Map<String, SearchCategory> categoriesMap, Map<String, SearchCategory> rootCategoriesMap) {

    TreeNode<OntologyNode> prunedOntologyTree = findPrunedOntology(ontology, "TranscriptRecordClasses.TranscriptRecordClass", scopes);

    // pass to mapper the categoriesMap to stuff the new categories into
    List<Map<String, SearchCategory>> mapList = new ArrayList<Map<String, SearchCategory>>();
    mapList.add(categoriesMap);
      
    // map the pruned ontology to a new root search category.  the root category holds the record class (for this scope)    
    SearchCategory rootCategory = prunedOntologyTree.mapStructure(new TreeNodeToSeachCategoryMapper(mapList, true));
    rootCategory.setDisplayName("Genes");
    rootCategory.setName("TranscriptRecordClasses.TranscriptRecordClass");
    rootCategoriesMap.put(rootCategory.getName(), rootCategory);
  }

  private TreeNode<OntologyNode> findPrunedOntology(Ontology ontology, String recordClassName, String scopes[]) {
    Predicate<OntologyNode> predicate = new IsSearchPredicate(recordClassName, scopes);
    return Ontology.getFilteredOntology(ontology, predicate, false) ;
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
      // the SearchCategory tree is non-uniform.  individuals are not nodes.
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
            if (isGenes) category.addChild(kid);
            
            // for non-genes, move question refs up to new search category, because we want to end up with only one level
            else for (CategoryQuestionRef qr : kid.getQuestionRefs()) category.addResolvedQuestionRef(qr);
          }
        }
      }
      
      if (category.getDisplayName() != null) {
        category.setName(category.getDisplayName().replaceAll(" ", "_"));
        category.setName(category.getName().replaceAll(",", "_"));
        for (Map<String, SearchCategory> map : maps)
          map.put(category.getName(), category);
      }
      return category;
    }
  }
   
  private class IsSearchPredicate implements Predicate<OntologyNode> {

    String scopes[];
    String recordClass;

    IsSearchPredicate(String recordClass, String scopes[]) {
      this.recordClass = recordClass;
      this.scopes = scopes;
    }

    @Override
      public boolean test(OntologyNode node) {
        boolean hasScope = false;
        for (String scope : scopes) if (node.containsKey("scope") && node.get("scope").contains(scope)) hasScope = true;
        return node.containsKey("targetType") && node.get("targetType").contains("search")
	  && node.containsKey("recordClassName") && node.get("recordClassName").contains(recordClass)
	  && hasScope;
      }
  }

}
