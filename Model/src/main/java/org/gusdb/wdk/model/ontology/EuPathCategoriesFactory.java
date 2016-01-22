package org.gusdb.wdk.model.ontology;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.gusdb.fgputil.functional.FunctionalInterfaces.Predicate;
import org.gusdb.fgputil.functional.TreeNode;
import org.gusdb.fgputil.functional.TreeNode.StructureMapper;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.question.CategoryQuestionRef;
import org.gusdb.wdk.model.question.SearchCategory;

public class EuPathCategoriesFactory {
  
  // scopes
  private final String INTERNAL = "internal";
  private final String MENU = "menu";
  private final String WEBSERVICE = "webservice";
  
  private Map<String, SearchCategory> websiteRootCategories = null;
  private Map<String, SearchCategory> webserviceRootCategories = null;
  private Map<String, SearchCategory> datasetRootCategories = null;
  WdkModel model;
  
  private String[] webSiteAndServiceClasses = {"CompoundRecordClasses.CompoundRecordClass", "DatasetRecordClasses.DatasetRecordClass", "DynSpanRecordClasses.DynSpanRecordClass", "EstRecordClasses.EstRecordClass", 
      "IsolateRecordClasses.IsolateRecordClass", "OrfRecordClasses.OrfRecordClass", "PathwayRecordClasses.PathwayRecordClass", "SequenceRecordClasses.SequenceRecordClass", "SnpChipRecordClasses.SnpChipRecordClass", "SnpRecordClasses.SnpRecordClass"};
  
  private String[] webServiceClasses = {"OrganismRecordClasses.OrganismRecordClass", "TranscriptRecordClasses.TranscriptRecordClass"};
  
  private String[] webSiteClasses = {"TranscriptRecordClasses.TranscriptRecordClass"};
  
  private String[] datasetClasses = {"TranscriptRecordClasses.TranscriptRecordClass"};
  
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
  
  private  void getCategories() throws WdkModelException {

    Ontology ontology = model.getOntology("Categories");

    // either webservice or website
    String[] scopes1 = { MENU, WEBSERVICE };
    for (String recordClassName : webSiteAndServiceClasses) {
      TreeNode<OntologyNode> prunedOntologyTree = findPrunedOntology(ontology, recordClassName, scopes1);
      SearchCategory rootCategory = prunedOntologyTree.mapStructure(new TreeNodeToSeachCategoryMapper());
      websiteRootCategories.put(rootCategory.getName(), rootCategory);
      webserviceRootCategories.put(rootCategory.getName(), rootCategory);
    }

    // website only
    String[] scopes2 = { MENU};
    for (String recordClassName : webSiteClasses) {
      TreeNode<OntologyNode> prunedOntologyTree = findPrunedOntology(ontology, recordClassName, scopes2);
      SearchCategory rootCategory = prunedOntologyTree.mapStructure(new TreeNodeToSeachCategoryMapper());
      websiteRootCategories.put(rootCategory.getName(), rootCategory);
    }

    // webservice only
    String[] scopes3 = { WEBSERVICE };
    for (String recordClassName : webServiceClasses) {
      TreeNode<OntologyNode> prunedOntologyTree = findPrunedOntology(ontology, recordClassName, scopes3);
      SearchCategory rootCategory = prunedOntologyTree.mapStructure(new TreeNodeToSeachCategoryMapper());
      webserviceRootCategories.put(rootCategory.getName(), rootCategory);
    }

    // dataset only
    String[] scopes4 = { INTERNAL };
    for (String recordClassName : datasetClasses) {
      TreeNode<OntologyNode> prunedOntologyTree = findPrunedOntology(ontology, recordClassName, scopes4);
      SearchCategory rootCategory = prunedOntologyTree.mapStructure(new TreeNodeToSeachCategoryMapper());
      webserviceRootCategories.put(rootCategory.getName(), rootCategory);
    }
  }

  private TreeNode<OntologyNode> findPrunedOntology(Ontology ontology, String recordClassName, String scopes[]) {
    Predicate<OntologyNode> predicate = new IsSearchPredicate(recordClassName, scopes);
    return Ontology.getFilteredOntology(ontology, predicate, true) ;
  }
  
  private class TreeNodeToSeachCategoryMapper implements StructureMapper<OntologyNode, SearchCategory> {

    @Override
    public SearchCategory map(OntologyNode nodeContents, List<SearchCategory> mappedChildren) {
      SearchCategory category = new SearchCategory();

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
        category.setName(nodeContents.get("label").get(0));
        category.setDisplayName(
            nodeContents.containsKey("displayName") ? nodeContents.get("displayName").get(0) : null);
        category.setShortDisplayName(nodeContents.containsKey("shortDisplayName")
            ? nodeContents.get("shortDisplayName").get(0) : null);
        for (SearchCategory kid : mappedChildren) {
          if (kid.isFlattenInMenu()) {  // an individual
            CategoryQuestionRef qr = new CategoryQuestionRef();
            qr.setText(kid.getName());
            qr.setUsedBy(kid.getUsedBy());
            category.addQuestionRef(qr);
          } else {
            category.addChild(kid);
          }
        }
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
        for (String scope : scopes) if (node.get("scope").contains(scope)) hasScope = true;
        return node.get("targetType").contains("search") && node.get("recordClass").contains(recordClass) && hasScope;
      }
  }

}
