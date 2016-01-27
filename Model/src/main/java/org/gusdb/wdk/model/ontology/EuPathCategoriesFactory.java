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

public class EuPathCategoriesFactory {
  
  @SuppressWarnings("unused")
  private static final Logger LOG = Logger.getLogger(EuPathCategoriesFactory.class);

  // scopes
  private final String INTERNAL = "internal";
  private final String MENU = "menu";
  private final String WEBSERVICE = "webservice";
  
  private Map<String, SearchCategory> websiteRootCategories = new LinkedHashMap<String, SearchCategory>();
  private Map<String, SearchCategory> webserviceRootCategories = new LinkedHashMap<String, SearchCategory>();
  private Map<String, SearchCategory> datasetRootCategories = new LinkedHashMap<String, SearchCategory>();
  private Map<String, SearchCategory> websiteCategories = new LinkedHashMap<String, SearchCategory>();
  private Map<String, SearchCategory> webserviceCategories = new LinkedHashMap<String, SearchCategory>();
  private Map<String, SearchCategory> datasetCategories = new LinkedHashMap<String, SearchCategory>();
  WdkModel model;
  
  private String[][] webSiteAndServiceClasses = {{"Isolates", "IsolateRecordClasses.IsolateRecordClass"}, {"Genomic Sequences", "SequenceRecordClasses.SequenceRecordClass"}, {"Genomic Segments", "DynSpanRecordClasses.DynSpanRecordClass"}, {"SNPs", "SnpRecordClasses.SnpRecordClass"}, {"SNPs (from Chips)", "SnpChipRecordClasses.SnpChipRecordClass"}, {"ESTs", "EstRecordClasses.EstRecordClass"}, {"ORFs", "OrfRecordClasses.OrfRecordClass"}, {"Metabolic Pathways", "PathwayRecordClasses.PathwayRecordClass"}, {"Compounds", "CompoundRecordClasses.CompoundRecordClass"} };
  
  //  private String[][] webServiceClasses = {{"Organisms", "OrganismRecordClasses.OrganismRecordClass"}, {"Genes", "TranscriptRecordClasses.TranscriptRecordClass"}};
  
  private String[][] webServiceClasses = {{"Genes", "TranscriptRecordClasses.TranscriptRecordClass"}};

  private String[][] webSiteClasses = {{"Genes", "TranscriptRecordClasses.TranscriptRecordClass"}};
  
  private String[][] datasetClasses = {{"Genes", "TranscriptRecordClasses.TranscriptRecordClass"}};
  
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
  
  private  void getCategories() throws WdkModelException {

    Ontology ontology = model.getOntology("Categories");

    // website only
    String[] scopes2 = { MENU};
    for (String[] recordClassInfo : webSiteClasses) {
      TreeNode<OntologyNode> prunedOntologyTree = findPrunedOntology(ontology, recordClassInfo[1], scopes2);
      List<Map<String, SearchCategory>> mapList =  new ArrayList<Map<String, SearchCategory>>();
      mapList.add(websiteCategories);
      SearchCategory rootCategory = prunedOntologyTree.mapStructure(new TreeNodeToSeachCategoryMapper(mapList));
      rootCategory.setDisplayName(recordClassInfo[0]);
      rootCategory.setName(recordClassInfo[1]);
      websiteRootCategories.put(rootCategory.getName(), rootCategory);
    }

    // webservice only
    String[] scopes3 = { WEBSERVICE };
    for (String[] recordClassInfo : webServiceClasses) {
      TreeNode<OntologyNode> prunedOntologyTree = findPrunedOntology(ontology, recordClassInfo[1], scopes3);
      List<Map<String, SearchCategory>> mapList =  new ArrayList<Map<String, SearchCategory>>();
      mapList.add(webserviceCategories);
      SearchCategory rootCategory = prunedOntologyTree.mapStructure(new TreeNodeToSeachCategoryMapper(mapList));
      rootCategory.setDisplayName(recordClassInfo[0]);
      rootCategory.setName(recordClassInfo[1]);
      webserviceRootCategories.put(rootCategory.getName(), rootCategory);
    }

    // either webservice or website
    String[] scopes1 = { MENU, WEBSERVICE };
    for (String[] recordClassInfo : webSiteAndServiceClasses) {
      TreeNode<OntologyNode> prunedOntologyTree = findPrunedOntology(ontology, recordClassInfo[1], scopes1);
      List<Map<String, SearchCategory>> mapList =  new ArrayList<Map<String, SearchCategory>>();
      mapList.add(webserviceCategories);
      mapList.add(websiteCategories);
      SearchCategory almostRootCategory = prunedOntologyTree.mapStructure(new TreeNodeToSeachCategoryMapper(mapList));
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

    // dataset only
    String[] scopes4 = { INTERNAL };
    for (String[] recordClassInfo : datasetClasses) {
      TreeNode<OntologyNode> prunedOntologyTree = findPrunedOntology(ontology, recordClassInfo[1], scopes4);
      List<Map<String, SearchCategory>> mapList =  new ArrayList<Map<String, SearchCategory>>();
      mapList.add(datasetCategories);
      SearchCategory rootCategory = prunedOntologyTree.mapStructure(new TreeNodeToSeachCategoryMapper(mapList));
      rootCategory.setDisplayName(recordClassInfo[0]);
      rootCategory.setName(recordClassInfo[1]);
      webserviceRootCategories.put(rootCategory.getName(), rootCategory);
    }
  }

  private TreeNode<OntologyNode> findPrunedOntology(Ontology ontology, String recordClassName, String scopes[]) {
    Predicate<OntologyNode> predicate = new IsSearchPredicate(recordClassName, scopes);
    return Ontology.getFilteredOntology(ontology, predicate, true) ;
  }
  
  private class TreeNodeToSeachCategoryMapper implements StructureMapper<OntologyNode, SearchCategory> {

    private List<Map<String, SearchCategory>> maps;

    TreeNodeToSeachCategoryMapper(List<Map<String, SearchCategory>> maps) {
      this.maps = maps;
    }

    @Override
    public SearchCategory map(OntologyNode nodeContents, List<SearchCategory> mappedChildren) {
      SearchCategory category = new SearchCategory();
      category.setWdkModel(model); // do this before adding question refs

      // make fake SearchCategory to hold an individual.  will fix when we build parent
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
      else {
        category.setDescription(
            nodeContents.containsKey("hasDefinition") ? nodeContents.get("hasDefinition").get(0) : null);
	String label = nodeContents.containsKey("label") ? nodeContents.get("label").get(0) : null;
	String name = nodeContents.containsKey("name") ? nodeContents.get("name").get(0) : label;
	String displayName = nodeContents.containsKey("displayName") ? nodeContents.get("displayName").get(0) : name;
	name = displayName;

        category.setName( name);
        category.setDisplayName(displayName);
        category.setShortDisplayName(nodeContents.containsKey("shortDisplayName")
            ? nodeContents.get("shortDisplayName").get(0) : null);
        for (SearchCategory kid : mappedChildren) {
          if (kid.isFlattenInMenu()) {  // an individual
            CategoryQuestionRef qr = new CategoryQuestionRef();
            qr.setText(kid.getName());
            qr.setUsedBy(kid.getUsedBy());
            category.addResolvedQuestionRef(qr);
          } else {
            category.addChild(kid);
          }
        }
      }
      if (category.getName() != null) {
	category.setName(category.getName().replaceAll(" ", "_"));
	for (Map<String, SearchCategory> map : maps) map.put(category.getName(), category);
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
