<%@ taglib prefix="site" tagdir="/WEB-INF/tags/site" %>
<%@ taglib prefix="wdkm" uri="http://www.gusdb.org/taglibs/wdk-misc-0.1" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<site:header banner="RNA View" />

<wdkm:renameAttribute from="ri" to="rna" />

<p>This page shows a RNA in detail (Custom view)
<hr>
<p>
<table width="100%">
  <tr><td><b>RNA ID</b></td> <td>${rna.rna_id}</td></tr>
  <tr><td><b>Organism</b></td>      <td>${rna.taxon_name}</td></tr>
  <tr><td><b>Gene Symbol</b></td>      <td>${rna.gene_symbol}</td></tr>
  <tr><td><b>Assembly Consistency</b></td>   <td>${rna.assembly_consistency}</td></tr>
  <tr><td><b>Contains mRNA?</b></td>      <td><wdkm:bt>${rna.contains_mrna}</wdkm:bt></td></tr>
  
</table>


<site:footer />
