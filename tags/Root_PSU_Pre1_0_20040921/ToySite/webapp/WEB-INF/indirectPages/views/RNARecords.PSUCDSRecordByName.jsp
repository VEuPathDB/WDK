<%@ taglib prefix="site" tagdir="/WEB-INF/tags/site" %>
<%@ taglib prefix="wdkm" uri="http://www.gusdb.org/taglibs/wdk-misc-0.1" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<site:header banner="CDS View" />

<wdkm:renameAttribute from="ri" to="cds" />

<p>This page shows a CDS in detail (Custom view)
<hr>
<p>
<table width="100%">
  <tr><td><b>Systematic id</b></td> <td>${cds.name}</td></tr>
  <tr><td><b>Product</b></td>       <td>${cds.product}</td></tr>
  <tr><td><b>Organism</b></td>      <td>${cds.taxon_name}</td></tr>
  <tr><td><b>GeneType</b></td>      <td>${cds.gene_type}</td></tr>
  <tr><td><b>Pseudogene?</b></td>   <td><wdkm:bt>${cds.is_pseudo}</wdkm:bt></td></tr>
  <tr><td><b>Partial?</b></td>      <td><wdkm:bt>${cds.is_partial}</wdkm:bt></td></tr>
</table>


<site:footer />
