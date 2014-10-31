# Notes for refactoring to AttributeFilter
See https://docs.google.com/a/apidb.org/drawings/d/1dPk91yndpsB800h4xr9OaJVudT65boDSDfz28KTnCKg/edit?usp=sharing


## AttributeFilter Views

FieldDetail      ->  AttributeDetail
FieldList        ->  AttributeList
FilterFields     ->  Attributes
FilterItems      ->  FilterSelection
FilterItem       ->  FilterItem
Filter           ->  AttributeFilter
MembershipFilter ->  MembershipFilter
RangeFilter      ->  RangeFilter
Results          ->  Result

### Hierarchy

- AttributeFilter
  - FilterSelection
    - FilterItem
  - Attribtues
    - AttributeList
    - AttributeDetail
      - MembershipFilter|RangeFilter
  - FilterResult

## Layout Views

FilterCollapsed  ->  n/a
FilterExpanded   ->  n/a
