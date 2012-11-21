package org.gusdb.wdk.model.jspwrap;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gusdb.wdk.model.query.param.AbstractEnumParam;
import org.gusdb.wdk.model.query.param.EnumParamTermNode;

public class EnumParamCache {
	
	//private static Logger logger = Logger.getLogger(EnumParamCache.class.getName());
	
	private AbstractEnumParam _source;
	private String _dependedValue;
	private String _defaultValue;
	private Map<String, String> _termInternalMap = new LinkedHashMap<String, String>();
	private Map<String, String> _termDisplayMap = new LinkedHashMap<String, String>();
	private Map<String, String> _termParentMap = new LinkedHashMap<String, String>();
	private List<EnumParamTermNode> _termTreeList = new ArrayList<EnumParamTermNode>();
	
	public EnumParamCache(AbstractEnumParam source, String dependedValue) {
		_source = source;
		_dependedValue = dependedValue;
	}
	
	public String getDefaultValue() {
		return _defaultValue;
	}
	
	public void setDefaultValue(String defaultValue) {
		_defaultValue = defaultValue;
	}

	public void addTermValues(String term, String internalVal, String displayVal, String parentTerm) {
		if (internalVal == null || displayVal == null /*|| parentTerm == null*/ ) {
			StringBuilder badVals = new StringBuilder();
			badVals.append(internalVal == null ? ",internal " : "");
			badVals.append(displayVal == null ? ",display " : "");
			//badVals.append(parentTerm == null ? ",parent " : "");
			throw new IllegalArgumentException("Null { " + badVals.toString().substring(1) + "} value(s) found for term " + term);
		}
		// strip off the comma from term
        term = term.replaceAll(",", " -");
        term = term.replaceAll("`", "'");
        term = term.replaceAll("``", "\"");
		
		_termInternalMap.put(term, internalVal);
		_termDisplayMap.put(term, displayVal);
		_termParentMap.put(term, parentTerm);
	}
	
	public boolean isEmpty() {
		// all maps should contain the same keys (except top level nodes will have null parents)
		return _termInternalMap.isEmpty();
	}
	
	public int getNumTerms() {
		return _termInternalMap.size();
	}

	public Set<String> getTerms() {
		return new LinkedHashSet<String>(_termInternalMap.keySet());
	}

	public boolean containsTerm(String term) {
		return _termInternalMap.containsKey(term);
	}
	
	public String getInternal(String term) { return _termInternalMap.get(term); }
	public String getDisplay(String term) { return _termDisplayMap.get(term); }
	public String getParent(String term) { return _termParentMap.get(term); }
	
	public Map<String, String> getVocabMap() {
        return new LinkedHashMap<String, String>(_termInternalMap);
	}

	public Map<String, String> getDisplayMap() {
        return new LinkedHashMap<String, String>(_termDisplayMap);
	}

	public Map<String, String> getParentMap() {
        return new LinkedHashMap<String, String>(_termParentMap);
	}
	
	public String[] getVocab() {
        String[] array = new String[_termInternalMap.size()];
        _termInternalMap.keySet().toArray(array);
        return array;
	}

	public String[] getDisplays() {
        String[] displays = new String[_termDisplayMap.size()];
        _termDisplayMap.values().toArray(displays);
        return displays;
	}

	public EnumParamTermNode[] getVocabTreeRoots() {
        if (_termTreeList != null) {
            EnumParamTermNode[] array = new EnumParamTermNode[_termTreeList.size()];
            _termTreeList.toArray(array);
            return array;
        }
        return new EnumParamTermNode[0];
	}

	public String[] getVocabInternal() {
        String[] array = new String[_termInternalMap.size()];
        if (_source.isNoTranslation()) _termInternalMap.keySet().toArray(array);
        else _termInternalMap.values().toArray(array);
        return array;
	}

	public void addParentNodeToTree(EnumParamTermNode node) {
		_termTreeList.add(node);
	}

	public void unsetParentTerm(String term) {
		_termParentMap.remove(term);
	}

	public List<EnumParamTermNode> getTermTreeListRef() {
		return _termTreeList;
	}

	public String getDependedValue() {
		return _dependedValue;
	}
	
	public void removeTerm(String term) {
	  // before removing the term, need to shortcut the children to its parent
	  String parent = _termParentMap.get(term);
	  for (String child : _termParentMap.keySet()) {
	    if (term.equals(_termParentMap.get(child)))
	      _termParentMap.put(child, parent);
	  }
	  
	  _termDisplayMap.remove(term);
	  _termInternalMap.remove(term);
	  _termParentMap.remove(term);
	}
}
