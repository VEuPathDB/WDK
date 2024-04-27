package org.gusdb.wdk.model.fix.table.edaanalysis.plugins;

import org.gusdb.wdk.model.fix.table.TableRowInterfaces;
import org.gusdb.wdk.model.fix.table.edaanalysis.AnalysisRow;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class CorrelationRefactorPluginTest {

  @Test
  public void test() throws Exception {
    CorrelationRefactorPlugin plugin = new CorrelationRefactorPlugin();
    TableRowInterfaces.RowResult<AnalysisRow> result = plugin.processRecord(new AnalysisRow("id", "id", new JSONObject(testConfig()), 3, 3, 3));
    String descriptorAsString = result.getRow().getDescriptor().toString();
    Assert.assertTrue(descriptorAsString.contains("data1"));
    Assert.assertTrue(descriptorAsString.contains("data2"));
  }

  private static String testConfig() {
    return "{\n" +
        "  \"subset\": {\n" +
        "    \"descriptor\": [],\n" +
        "    \"uiSettings\": {}\n" +
        "  },\n" +
        "  \"computations\": [\n" +
        "    {\n" +
        "      \"computationId\": \"iopsx\",\n" +
        "      \"descriptor\": {\n" +
        "        \"type\": \"correlationassaymetadata\",\n" +
        "        \"configuration\": {\n" +
        "          \"prefilterThresholds\": {\n" +
        "            \"proportionNonZero\": 0.05,\n" +
        "            \"variance\": 0,\n" +
        "            \"standardDeviation\": 0\n" +
        "          },\n" +
        "          \"collectionVariable\": {\n" +
        "            \"entityId\": \"EUPATH_0000808\",\n" +
        "            \"collectionId\": \"EUPATH_0009256\"\n" +
        "          },\n" +
        "          \"correlationMethod\": \"spearman\"\n" +
        "        }\n" +
        "      },\n" +
        "      \"visualizations\": [\n" +
        "        {\n" +
        "          \"visualizationId\": \"e7411924-0ecb-48c9-9a8f-f8ea3ed73468\",\n" +
        "          \"displayName\": \"Taxa correlated with age\",\n" +
        "          \"descriptor\": {\n" +
        "            \"type\": \"bipartitenetwork\",\n" +
        "            \"configuration\": {\n" +
        "              \"correlationCoefThreshold\": 0.5,\n" +
        "              \"significanceThreshold\": 0.05\n" +
        "            },\n" +
        "            \"currentPlotFilters\": []\n" +
        "          }\n" +
        "        }\n" +
        "      ]\n" +
        "    },\n" +
        "    {\n" +
        "      \"computationId\": \"jwdb9\",\n" +
        "      \"descriptor\": {\n" +
        "        \"type\": \"abundance\",\n" +
        "        \"configuration\": {\n" +
        "          \"collectionVariable\": {\n" +
        "            \"collectionId\": \"EUPATH_0009256\",\n" +
        "            \"entityId\": \"EUPATH_0000808\"\n" +
        "          },\n" +
        "          \"rankingMethod\": \"variance\"\n" +
        "        }\n" +
        "      },\n" +
        "      \"visualizations\": [\n" +
        "        {\n" +
        "          \"visualizationId\": \"6e6afcbf-fbf0-4d3c-bfa3-ab7bbc92dda9\",\n" +
        "          \"displayName\": \"Top taxa by age (genus); faceted by case/control status\",\n" +
        "          \"descriptor\": {\n" +
        "            \"type\": \"boxplot\",\n" +
        "            \"configuration\": {\n" +
        "              \"dependentAxisValueSpec\": \"Full\",\n" +
        "              \"overlayVariable\": {\n" +
        "                \"entityId\": \"EUPATH_0000096\",\n" +
        "                \"variableId\": \"EUPATH_0051008\"\n" +
        "              },\n" +
        "              \"facetVariable\": {\n" +
        "                \"entityId\": \"EUPATH_0000096\",\n" +
        "                \"variableId\": \"EUPATH_0010375\"\n" +
        "              }\n" +
        "            },\n" +
        "            \"currentPlotFilters\": []\n" +
        "          }\n" +
        "        }\n" +
        "      ]\n" +
        "    },\n" +
        "    {\n" +
        "      \"computationId\": \"pia0q\",\n" +
        "      \"descriptor\": {\n" +
        "        \"type\": \"alphadiv\",\n" +
        "        \"configuration\": {\n" +
        "          \"collectionVariable\": {\n" +
        "            \"collectionId\": \"EUPATH_0009256\",\n" +
        "            \"entityId\": \"EUPATH_0000808\"\n" +
        "          },\n" +
        "          \"alphaDivMethod\": \"shannon\"\n" +
        "        }\n" +
        "      },\n" +
        "      \"visualizations\": [\n" +
        "        {\n" +
        "          \"visualizationId\": \"e7d48cfb-aeb4-4f2a-9c68-699a27ebefa6\",\n" +
        "          \"displayName\": \"Alpha div (genus) by age w/ case/control status as overlay\",\n" +
        "          \"descriptor\": {\n" +
        "            \"type\": \"boxplot\",\n" +
        "            \"configuration\": {\n" +
        "              \"dependentAxisValueSpec\": \"Full\",\n" +
        "              \"xAxisVariable\": {\n" +
        "                \"entityId\": \"EUPATH_0000096\",\n" +
        "                \"variableId\": \"EUPATH_0051008\"\n" +
        "              },\n" +
        "              \"overlayVariable\": {\n" +
        "                \"entityId\": \"EUPATH_0000096\",\n" +
        "                \"variableId\": \"EUPATH_0010375\"\n" +
        "              }\n" +
        "            },\n" +
        "            \"currentPlotFilters\": []\n" +
        "          }\n" +
        "        }\n" +
        "      ]\n" +
        "    }\n" +
        "  ],\n" +
        "  \"starredVariables\": [\n" +
        "    {\n" +
        "      \"entityId\": \"EUPATH_0000096\",\n" +
        "      \"variableId\": \"EUPATH_0010375\"\n" +
        "    },\n" +
        "    {\n" +
        "      \"entityId\": \"EUPATH_0000096\",\n" +
        "      \"variableId\": \"EUPATH_0010367\"\n" +
        "    }\n" +
        "  ],\n" +
        "  \"dataTableConfig\": {},\n" +
        "  \"derivedVariables\": []\n" +
        "}";
  }
}
