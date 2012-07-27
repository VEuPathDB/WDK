wdk.util.namespace("wdk.test.strategy", function(exports) {

  var testData = {
    "type": "success",
    "state": {
      "1": {
        "id": 1,
        "checksum": "59401bb222922a61a13f1a224391ab39"
      },
      "length": 1,
      "count": 1
    },
    "currentView": {
      "strategy": "1",
      "step": 12,
      "pagerOffset": 0
    },
    "strategies": {
      "59401bb222922a61a13f1a224391ab39": {
        "name": "Putative drug targets",
        "id": "1",
        "saved": false,
        "importId": "1a0887fc79fcd678",
        "isValid": true,
        "steps": {
          "1": {
            "name": "EC Number",
            "customName": "EC Number",
            "id": 1,
            "answerId": 5408660,
            "isCollapsed": false,
            "isUncollapsible": false,
            "dataType": "GeneRecordClasses.GeneRecordClass",
            "displayType": "Gene",
            "shortDisplayType": "Gene",
            "shortName": "EC Number",
            "results": 504,
            "questionName": "GeneQuestions.GenesByEcNumber",
            "displayName": "EC Number",
            "isboolean": false,
            "istransform": false,
            "filtered": false,
            "filterName": "All Results",
            "urlParams": "&array%28organism%29=Plasmodium+falciparum&array%28organism%29=Plasmodium+yoelii&array%28organism%29=Plasmodium+knowlesi&array%28ec_number_pattern%29=2-",
            "isValid": true,
            "assignedWeight": 0,
            "useweights": true,
            "revisable": true,
            "frontId": 1,
            "params": [
              {
                "name": "organism",
                "prompt": "Organism",
                "visible": true,
                "className": "org.gusdb.wdk.model.jspwrap.EnumParamBean",
                "value": "Plasmodium falciparum, Plasmodium yoelii, Plasmodium knowlesi",
                "internal": "Plasmodium falciparum,Plasmodium yoelii,Plasmodium knowlesi"
              },
              {
                "name": "ec_number_pattern",
                "prompt": "EC Number (use * as wildcard)",
                "visible": true,
                "className": "org.gusdb.wdk.model.jspwrap.EnumParamBean",
                "value": "2-",
                "internal": "2-"
              }
            ]
          },
          "2": {
            "name": "Combine Gene results",
            "customName": "1 UNION 2",
            "id": 3,
            "answerId": 5408670,
            "isCollapsed": false,
            "isUncollapsible": false,
            "dataType": "GeneRecordClasses.GeneRecordClass",
            "displayType": "Gene",
            "shortDisplayType": "Gene",
            "shortName": "Combine Gene results",
            "results": 7911,
            "questionName": "InternalQuestions.boolean_question_GeneRecordClasses_GeneRecordClass",
            "displayName": "Combine Gene results",
            "isboolean": true,
            "istransform": false,
            "filtered": false,
            "filterName": "All Results",
            "urlParams": "&value%28bq_left_op_GeneRecordClasses_GeneRecordClass%29=1&value%28bq_right_op_GeneRecordClasses_GeneRecordClass%29=2&value%28bq_operator%29=UNION&value%28use_boolean_filter%29=false",
            "isValid": true,
            "assignedWeight": 0,
            "useweights": true,
            "revisable": true,
            "frontId": 2,
            "params": [
              {
                "name": "bq_left_op_GeneRecordClasses_GeneRecordClass",
                "prompt": "Left Operand",
                "visible": false,
                "className": "org.gusdb.wdk.model.jspwrap.AnswerParamBean",
                "value": "1",
                "internal": "1"
              },
              {
                "name": "bq_right_op_GeneRecordClasses_GeneRecordClass",
                "prompt": "Right Operand",
                "visible": false,
                "className": "org.gusdb.wdk.model.jspwrap.AnswerParamBean",
                "value": "2",
                "internal": "2"
              },
              {
                "name": "bq_operator",
                "prompt": "Operator",
                "visible": true,
                "className": "org.gusdb.wdk.model.jspwrap.StringParamBean",
                "value": "UNION",
                "internal": "UNION"
              },
              {
                "name": "use_boolean_filter",
                "prompt": "Use Expand Filter",
                "visible": true,
                "className": "org.gusdb.wdk.model.jspwrap.StringParamBean",
                "value": "false",
                "internal": "false"
              }
            ],
            "childrenCount": 2,
            "operation": "UNION",
            "step": {
              "name": "GO Term",
              "customName": "GO Term",
              "id": 2,
              "answerId": 3366740,
              "isCollapsed": false,
              "isUncollapsible": false,
              "dataType": "GeneRecordClasses.GeneRecordClass",
              "displayType": "Gene",
              "shortDisplayType": "Gene",
              "shortName": "GO Term",
              "results": 7851,
              "questionName": "GeneQuestions.GenesByGoTerm",
              "displayName": "GO Term",
              "isboolean": false,
              "istransform": false,
              "filtered": false,
              "filterName": "All Results",
              "urlParams": "&array%28go_term%29=catalytic+activity&array%28organism%29=%5BC%5Df48ba82288afd0a82d9a618ee368a2dc",
              "isValid": true,
              "assignedWeight": 0,
              "useweights": true,
              "revisable": true,
              "frontId": 1,
              "params": [
                {
                  "name": "organism",
                  "prompt": "Organism",
                  "visible": true,
                  "className": "org.gusdb.wdk.model.jspwrap.EnumParamBean",
                  "value": "Plasmodium falciparum, Plasmodium vivax, Plasmodium yoelii, Plasmodium berghei, Plasmodium chabaudi, Plasmodium knowlesi",
                  "internal": "Plasmodium falciparum,Plasmodium vivax,Plasmodium yoelii,Plasmodium berghei,Plasmodium chabaudi,Plasmodium knowlesi"
                },
                {
                  "name": "go_term",
                  "prompt": "GO Term or GO ID",
                  "visible": true,
                  "className": "org.gusdb.wdk.model.jspwrap.EnumParamBean",
                  "value": "catalytic activity",
                  "internal": "catalytic activity"
                }
              ]
            }
          },
          "3": {
            "name": "Combine Gene results",
            "customName": "3 INTERSECT 6",
            "id": 7,
            "answerId": 11389600,
            "isCollapsed": false,
            "isUncollapsible": false,
            "dataType": "GeneRecordClasses.GeneRecordClass",
            "displayType": "Gene",
            "shortDisplayType": "Gene",
            "shortName": "Combine Gene results",
            "results": 1380,
            "questionName": "InternalQuestions.boolean_question_GeneRecordClasses_GeneRecordClass",
            "displayName": "Combine Gene results",
            "isboolean": true,
            "istransform": false,
            "filtered": false,
            "filterName": "All Results",
            "urlParams": "&value%28bq_left_op_GeneRecordClasses_GeneRecordClass%29=3&value%28bq_right_op_GeneRecordClasses_GeneRecordClass%29=6&value%28bq_operator%29=INTERSECT&value%28use_boolean_filter%29=false",
            "isValid": true,
            "assignedWeight": 0,
            "useweights": true,
            "revisable": true,
            "frontId": 3,
            "params": [
              {
                "name": "bq_left_op_GeneRecordClasses_GeneRecordClass",
                "prompt": "Left Operand",
                "visible": false,
                "className": "org.gusdb.wdk.model.jspwrap.AnswerParamBean",
                "value": "3",
                "internal": "3"
              },
              {
                "name": "bq_right_op_GeneRecordClasses_GeneRecordClass",
                "prompt": "Right Operand",
                "visible": false,
                "className": "org.gusdb.wdk.model.jspwrap.AnswerParamBean",
                "value": "6",
                "internal": "6"
              },
              {
                "name": "bq_operator",
                "prompt": "Operator",
                "visible": true,
                "className": "org.gusdb.wdk.model.jspwrap.StringParamBean",
                "value": "INTERSECT",
                "internal": "INTERSECT"
              },
              {
                "name": "use_boolean_filter",
                "prompt": "Use Expand Filter",
                "visible": true,
                "className": "org.gusdb.wdk.model.jspwrap.StringParamBean",
                "value": "false",
                "internal": "false"
              }
            ],
            "childrenCount": 2,
            "operation": "INTERSECT",
            "step": {
              "name": "Combine Gene results",
              "customName": "4 UNION 5",
              "id": 6,
              "answerId": 11389590,
              "isCollapsed": true,
              "isUncollapsible": false,
              "dataType": "GeneRecordClasses.GeneRecordClass",
              "displayType": "Gene",
              "shortDisplayType": "Gene",
              "shortName": "Combine Gene results",
              "results": 4070,
              "questionName": "InternalQuestions.boolean_question_GeneRecordClasses_GeneRecordClass",
              "displayName": "Combine Gene results",
              "isboolean": true,
              "istransform": false,
              "filtered": false,
              "filterName": "All Results",
              "urlParams": "&value%28bq_left_op_GeneRecordClasses_GeneRecordClass%29=4&value%28bq_right_op_GeneRecordClasses_GeneRecordClass%29=5&value%28bq_operator%29=UNION&value%28use_boolean_filter%29=false",
              "isValid": true,
              "assignedWeight": 0,
              "useweights": true,
              "revisable": true,
              "frontId": 2,
              "params": [
                {
                  "name": "bq_left_op_GeneRecordClasses_GeneRecordClass",
                  "prompt": "Left Operand",
                  "visible": false,
                  "className": "org.gusdb.wdk.model.jspwrap.AnswerParamBean",
                  "value": "4",
                  "internal": "4"
                },
                {
                  "name": "bq_right_op_GeneRecordClasses_GeneRecordClass",
                  "prompt": "Right Operand",
                  "visible": false,
                  "className": "org.gusdb.wdk.model.jspwrap.AnswerParamBean",
                  "value": "5",
                  "internal": "5"
                },
                {
                  "name": "bq_operator",
                  "prompt": "Operator",
                  "visible": true,
                  "className": "org.gusdb.wdk.model.jspwrap.StringParamBean",
                  "value": "UNION",
                  "internal": "UNION"
                },
                {
                  "name": "use_boolean_filter",
                  "prompt": "Use Expand Filter",
                  "visible": true,
                  "className": "org.gusdb.wdk.model.jspwrap.StringParamBean",
                  "value": "false",
                  "internal": "false"
                }
              ],
              "strategy": {
                "name": "P.f. Expression Timing",
                "id": "1_6",
                "saved": "false",
                "savedName": "P.f. Expression Timing",
                "importId": "",
                "order": 1,
                "steps": {
                  "1": {
                    "name": "P.f. Intraerythrocytic Infection Cycle (fold change)",
                    "customName": "PfiRBC 48HR FC",
                    "id": 4,
                    "answerId": 11389480,
                    "isCollapsed": false,
                    "isUncollapsible": false,
                    "dataType": "GeneRecordClasses.GeneRecordClass",
                    "displayType": "Gene",
                    "shortDisplayType": "Gene",
                    "shortName": "PfiRBC 48HR FC",
                    "results": 997,
                    "questionName": "GeneQuestions.GenesByExpressionTiming",
                    "displayName": "P.f. Intraerythrocytic Infection Cycle (fold change)",
                    "isboolean": false,
                    "istransform": false,
                    "filtered": false,
                    "filterName": "All Results",
                    "urlParams": "&array%28global_min_max%29=Don%27t+care&array%28min_max_avg_comp%29=maximum1&array%28profileset_generic%29=DeRisi+3D7+Smoothed&array%28min_max_avg_ref%29=minimum1&array%28protein_coding_only%29=yes&array%28regulated_dir%29=up-regulated&value%28fold_change%29=2&array%28samples_fc_comp_generic%29=%5BC%5Df7e9ca453747bec6d95f654cb81d3460&array%28samples_fc_ref_generic%29=%5BC%5D75453b61264c8a145da8a4a8f5d1938a",
                    "isValid": true,
                    "assignedWeight": 10,
                    "useweights": true,
                    "revisable": true,
                    "frontId": 1,
                    "params": [
                      {
                        "name": "profileset_generic",
                        "prompt": "Experiment",
                        "visible": true,
                        "className": "org.gusdb.wdk.model.jspwrap.EnumParamBean",
                        "value": "iRBC 3D7 (48 Hour scaled)",
                        "internal": "DeRisi 3D7 Smoothed"
                      },
                      {
                        "name": "regulated_dir",
                        "prompt": "Direction",
                        "visible": true,
                        "className": "org.gusdb.wdk.model.jspwrap.EnumParamBean",
                        "value": "up-regulated",
                        "internal": "up-regulated"
                      },
                      {
                        "name": "samples_fc_ref_generic",
                        "prompt": "Reference Samples",
                        "visible": true,
                        "className": "org.gusdb.wdk.model.jspwrap.EnumParamBean",
                        "value": "1-16 Hours, 1-8 Hours, 1 Hour, 2 Hour, 3 Hour, 4 Hour, 5 Hour, 6 Hour, 7 Hour, 8 Hour, 9-16 Hours, 9 Hour, 10 Hour, 11 Hour, 12 Hour, 13 Hour, 14 Hour, 15 Hour, 16 Hour, 31-48 Hours, 31-39 Hours, 31 Hour, 32 Hour, 33 Hour, 34 Hour, 35 Hour, 36 Hour, 37 Hour, 38 Hour, 39 Hour, 40-48 Hours, 40 Hour, 41 Hour, 42 Hour, 43 Hour, 44 Hour, 45 Hour, 46 Hour",
                        "internal": "Ring,Early Ring,1 Hour,2 Hour,3 Hour,4 Hour,5 Hour,6 Hour,7 Hour,8 Hour,Late Ring,9 Hour,10 Hour,11 Hour,12 Hour,13 Hour,14 Hour,15 Hour,16 Hour,Schizont,Early Schizont,31 Hour,32 Hour,33 Hour,34 Hour,35 Hour,36 Hour,37 Hour,38 Hour,39 Hour,Late Schizont,40 Hour,41 Hour,42 Hour,43 Hour,44 Hour,45 Hour,46 Hour"
                      },
                      {
                        "name": "min_max_avg_ref",
                        "prompt": "Operation Applied to Reference Samples",
                        "visible": true,
                        "className": "org.gusdb.wdk.model.jspwrap.EnumParamBean",
                        "value": "minimum",
                        "internal": "minimum1"
                      },
                      {
                        "name": "samples_fc_comp_generic",
                        "prompt": "Comparison Samples",
                        "visible": true,
                        "className": "org.gusdb.wdk.model.jspwrap.EnumParamBean",
                        "value": "17-30 Hours, 17-23 Hours, 17 Hour, 18 Hour, 19 Hour, 20 Hour, 21 Hour, 22 Hour, 23 Hour, 24-30 Hours, 24 Hour, 25 Hour, 26 Hour, 27 Hour, 28 Hour, 29 Hour, 30 Hour",
                        "internal": "Trophozoite,Early Trophozoite,17 Hour,18 Hour,19 Hour,20 Hour,21 Hour,22 Hour,23 Hour,Late Trophozoite,24 Hour,25 Hour,26 Hour,27 Hour,28 Hour,29 Hour,30 Hour"
                      },
                      {
                        "name": "min_max_avg_comp",
                        "prompt": "Operation Applied to Comparison Samples",
                        "visible": true,
                        "className": "org.gusdb.wdk.model.jspwrap.EnumParamBean",
                        "value": "maximum",
                        "internal": "maximum1"
                      },
                      {
                        "name": "fold_change",
                        "prompt": "Fold change >=",
                        "visible": true,
                        "className": "org.gusdb.wdk.model.jspwrap.StringParamBean",
                        "value": "2",
                        "internal": "2"
                      },
                      {
                        "name": "global_min_max",
                        "prompt": "Global min / max in selected time points",
                        "visible": true,
                        "className": "org.gusdb.wdk.model.jspwrap.EnumParamBean",
                        "value": "Don't care",
                        "internal": "Don't care"
                      },
                      {
                        "name": "protein_coding_only",
                        "prompt": "Protein Coding Only:",
                        "visible": true,
                        "className": "org.gusdb.wdk.model.jspwrap.EnumParamBean",
                        "value": "yes",
                        "internal": "yes"
                      }
                    ]
                  },
                  "2": {
                    "name": "Combine Gene results",
                    "customName": "4 UNION 5",
                    "id": 6,
                    "answerId": 11389590,
                    "isCollapsed": false,
                    "isUncollapsible": false,
                    "dataType": "GeneRecordClasses.GeneRecordClass",
                    "displayType": "Gene",
                    "shortDisplayType": "Gene",
                    "shortName": "Combine Gene results",
                    "results": 4070,
                    "questionName": "InternalQuestions.boolean_question_GeneRecordClasses_GeneRecordClass",
                    "displayName": "Combine Gene results",
                    "isboolean": true,
                    "istransform": false,
                    "filtered": false,
                    "filterName": "All Results",
                    "urlParams": "&value%28bq_left_op_GeneRecordClasses_GeneRecordClass%29=4&value%28bq_right_op_GeneRecordClasses_GeneRecordClass%29=5&value%28bq_operator%29=UNION&value%28use_boolean_filter%29=false",
                    "isValid": true,
                    "assignedWeight": 0,
                    "useweights": true,
                    "revisable": true,
                    "frontId": 2,
                    "params": [
                      {
                        "name": "bq_left_op_GeneRecordClasses_GeneRecordClass",
                        "prompt": "Left Operand",
                        "visible": false,
                        "className": "org.gusdb.wdk.model.jspwrap.AnswerParamBean",
                        "value": "4",
                        "internal": "4"
                      },
                      {
                        "name": "bq_right_op_GeneRecordClasses_GeneRecordClass",
                        "prompt": "Right Operand",
                        "visible": false,
                        "className": "org.gusdb.wdk.model.jspwrap.AnswerParamBean",
                        "value": "5",
                        "internal": "5"
                      },
                      {
                        "name": "bq_operator",
                        "prompt": "Operator",
                        "visible": true,
                        "className": "org.gusdb.wdk.model.jspwrap.StringParamBean",
                        "value": "UNION",
                        "internal": "UNION"
                      },
                      {
                        "name": "use_boolean_filter",
                        "prompt": "Use Expand Filter",
                        "visible": true,
                        "className": "org.gusdb.wdk.model.jspwrap.StringParamBean",
                        "value": "false",
                        "internal": "false"
                      }
                    ],
                    "childrenCount": 2,
                    "operation": "UNION",
                    "step": {
                      "name": "Mass Spec. Evidence",
                      "customName": "Mass Spec",
                      "id": 5,
                      "answerId": 9562000,
                      "isCollapsed": false,
                      "isUncollapsible": false,
                      "dataType": "GeneRecordClasses.GeneRecordClass",
                      "displayType": "Gene",
                      "shortDisplayType": "Gene",
                      "shortName": "Mass Spec",
                      "results": 3957,
                      "questionName": "GeneQuestions.GenesByMassSpec",
                      "displayName": "Mass Spec. Evidence",
                      "isboolean": false,
                      "istransform": false,
                      "filtered": false,
                      "filterName": "All Results",
                      "urlParams": "&value%28min_spectrum_count%29=1&value%28min_sequence_count%29=1&array%28ms_assay%29=%5BC%5D595ed616e1436a6913cc19631915f100",
                      "isValid": true,
                      "assignedWeight": 10,
                      "useweights": true,
                      "revisable": true,
                      "frontId": 1,
                      "params": [
                        {
                          "name": "ms_assay",
                          "prompt": "Experiment/Samples",
                          "visible": true,
                          "className": "org.gusdb.wdk.model.jspwrap.EnumParamBean",
                          "value": "<i>Plasmodium falciparum</i>, <b>Blood stage phosphoproteome and total proteome (3D7) (Treeck & Sanders et al.)</b>, schizont phosphopeptide-depleted, schizont phosphopeptide-enriched, <b>Gametocytogenesis: trophozoites vs early or late gametocytes (3D7,NF54) (Silverstrini et al.)</b>, stage I-II gametocytes (3D7), stage V gametocytes (NF54), synchronous trophozoites from gametocyte-less 3D7A clone F12, <b>Merozoite Proteome (3D7) (Leiden Malaria Group unpublished)</b>, merozoite proteome (3D7), <b>P. falciparum cytoplasmic and nuclear fractions from rings, trophozoites and schizonts (3D7) (Oehring and Woodcroft et al. - unpublished)</b>, Ring stage fraction 1, Ring stage fraction 2, Ring stage fraction 3, Ring stage fraction 4, Ring stage fraction 5, Trophozoite stage fraction 1, Trophozoite stage fraction 2, Trophozoite stage fraction 3, Trophozoite stage fraction 4, Trophozoite stage fraction 5, Schizont stage fraction 1, Schizont stage fraction 2, Schizont stage fraction 3, Schizont stage fraction 4, Schizont stage fraction 5, <b>P. falciparum infected erythrocytes from patient blood sample (Acharya et al.)</b>, P. falciparum infected erythrocytes from patient blood sample, <b>Parasite -infected erythrocyte surface proteins (PIESP) (3D7) (Florens et al. 2004)</b>, late trophozoite-schizont stage biotin-purified iRBC membrane proteins, <b>Parasite rupture from erythrocyte (D10) (Bowyer et al.)</b>, schizont, 42 h post-infection, schizont, 48 h post-infection, <b>Profile of Sporozoite Maturation (NF54) (Lasonder et al.)</b>, oocyst-derived sporozoite 13-14 days post-infection, oocysts 7-8 days post-infection, salivary gland sporozoites 18-22 days post-infection, <b>Sexual and asexual life cycle stages (3D7) (Florens et al. 2002)</b>, gametocyte stage IV and V, merozoite (synchronized), sporozoite, 14 day purified, trophozoite (synchronized)",
                          "internal": "<i>Plasmodium falciparum</i>,<b>Blood stage phosphoproteome and total proteome (3D7) (Treeck & Sanders et al.)</b>,schizont phosphopeptide-depleted,schizont phosphopeptide-enriched,<b>Gametocytogenesis: trophozoites vs early or late gametocytes (3D7NF54) (Silverstrini et al.)</b>,stage I-II gametocytes (3D7),stage V gametocytes (NF54),synchronous trophozoites from gametocyte-less 3D7A clone F12,<b>Merozoite Proteome (3D7) (Leiden Malaria Group unpublished)</b>,merozoite proteome (3D7),<b>P. falciparum cytoplasmic and nuclear fractions from rings trophozoites and schizonts (3D7) (Oehring and Woodcroft et al. - unpublished)</b>,Ring stage fraction 1,Ring stage fraction 2,Ring stage fraction 3,Ring stage fraction 4,Ring stage fraction 5,Trophozoite stage fraction 1,Trophozoite stage fraction 2,Trophozoite stage fraction 3,Trophozoite stage fraction 4,Trophozoite stage fraction 5,Schizont stage fraction 1,Schizont stage fraction 2,Schizont stage fraction 3,Schizont stage fraction 4,Schizont stage fraction 5,<b>P. falciparum infected erythrocytes from patient blood sample (Acharya et al.)</b>,P. falciparum infected erythrocytes from patient blood sample,<b>Parasite -infected erythrocyte surface proteins (PIESP) (3D7) (Florens et al. 2004)</b>,late trophozoite-schizont stage biotin-purified iRBC membrane proteins,<b>Parasite rupture from erythrocyte (D10) (Bowyer et al.)</b>,schizont 42 h post-infection,schizont 48 h post-infection,<b>Profile of Sporozoite Maturation (NF54) (Lasonder et al.)</b>,oocyst-derived sporozoite 13-14 days post-infection,oocysts 7-8 days post-infection,salivary gland sporozoites 18-22 days post-infection,<b>Sexual and asexual life cycle stages (3D7) (Florens et al. 2002)</b>,gametocyte stage IV and V,merozoite (synchronized),sporozoite 14 day purified,trophozoite (synchronized)"
                        },
                        {
                          "name": "min_sequence_count",
                          "prompt": "Minimum Number of<br>Unique Peptide Sequences",
                          "visible": true,
                          "className": "org.gusdb.wdk.model.jspwrap.StringParamBean",
                          "value": "1",
                          "internal": "1"
                        },
                        {
                          "name": "min_spectrum_count",
                          "prompt": "Minimum Number of Spectra",
                          "visible": true,
                          "className": "org.gusdb.wdk.model.jspwrap.StringParamBean",
                          "value": "1",
                          "internal": "1"
                        }
                      ]
                    }
                  },
                  "length": 2,
                  "nonTransformLength": 2
                }
              }
            }
          },
          "4": {
            "name": "Combine Gene results",
            "customName": "7 INTERSECT 8",
            "id": 9,
            "answerId": 11389610,
            "isCollapsed": false,
            "isUncollapsible": false,
            "dataType": "GeneRecordClasses.GeneRecordClass",
            "displayType": "Gene",
            "shortDisplayType": "Gene",
            "shortName": "Combine Gene results",
            "results": 496,
            "questionName": "InternalQuestions.boolean_question_GeneRecordClasses_GeneRecordClass",
            "displayName": "Combine Gene results",
            "isboolean": true,
            "istransform": false,
            "filtered": true,
            "filterName": "Ortholog Groups",
            "urlParams": "&value%28bq_left_op_GeneRecordClasses_GeneRecordClass%29=7&value%28bq_right_op_GeneRecordClasses_GeneRecordClass%29=8&value%28bq_operator%29=INTERSECT&value%28use_boolean_filter%29=false",
            "isValid": true,
            "assignedWeight": 0,
            "useweights": true,
            "revisable": true,
            "frontId": 4,
            "params": [
              {
                "name": "bq_left_op_GeneRecordClasses_GeneRecordClass",
                "prompt": "Left Operand",
                "visible": false,
                "className": "org.gusdb.wdk.model.jspwrap.AnswerParamBean",
                "value": "7",
                "internal": "7"
              },
              {
                "name": "bq_right_op_GeneRecordClasses_GeneRecordClass",
                "prompt": "Right Operand",
                "visible": false,
                "className": "org.gusdb.wdk.model.jspwrap.AnswerParamBean",
                "value": "8",
                "internal": "8"
              },
              {
                "name": "bq_operator",
                "prompt": "Operator",
                "visible": true,
                "className": "org.gusdb.wdk.model.jspwrap.StringParamBean",
                "value": "INTERSECT",
                "internal": "INTERSECT"
              },
              {
                "name": "use_boolean_filter",
                "prompt": "Use Expand Filter",
                "visible": true,
                "className": "org.gusdb.wdk.model.jspwrap.StringParamBean",
                "value": "false",
                "internal": "false"
              }
            ],
            "childrenCount": 2,
            "operation": "INTERSECT",
            "step": {
              "name": "Orthology Phylogenetic Profile",
              "customName": "Ortho Ph Pro",
              "id": 8,
              "answerId": 5408500,
              "isCollapsed": false,
              "isUncollapsible": false,
              "dataType": "GeneRecordClasses.GeneRecordClass",
              "displayType": "Gene",
              "shortDisplayType": "Gene",
              "shortName": "Ortho Ph Pro",
              "results": 17194,
              "questionName": "GeneQuestions.GenesByOrthologPattern",
              "displayName": "Orthology Phylogenetic Profile",
              "isboolean": false,
              "istransform": false,
              "filtered": false,
              "filterName": "All Results",
              "urlParams": "&value%28excluded_species%29=n%2Fa&value%28included_species%29=n%2Fa&array%28phyletic_indent_map%29=ARCH&array%28organism%29=%5BC%5Df48ba82288afd0a82d9a618ee368a2dc&value%28profile_pattern%29=%25hsap%3AN%25mmus%3AN%25pber%3AY%25pcha%3AY%25pfal%3AY%25pkno%3AY%25pviv%3AY%25pyoe%3AY%25rnor%3AN%25&array%28phyletic_term_map%29=rnor",
              "isValid": true,
              "assignedWeight": 0,
              "useweights": true,
              "revisable": true,
              "frontId": 1,
              "params": [
                {
                  "name": "profile_pattern",
                  "prompt": "Profile Pattern",
                  "visible": false,
                  "className": "org.gusdb.wdk.model.jspwrap.StringParamBean",
                  "value": "%hsap:N%mmus:N%pber:Y%pcha:Y%pfal:Y%pkno:Y%pviv:Y%pyoe:Y%rnor:N%",
                  "internal": "%hsap:N%mmus:N%pber:Y%pcha:Y%pfal:Y%pkno:Y%pviv:Y%pyoe:Y%rnor:N%"
                },
                {
                  "name": "included_species",
                  "prompt": "Included Species",
                  "visible": true,
                  "className": "org.gusdb.wdk.model.jspwrap.StringParamBean",
                  "value": "n/a",
                  "internal": "n/a"
                },
                {
                  "name": "excluded_species",
                  "prompt": "Excluded Species",
                  "visible": true,
                  "className": "org.gusdb.wdk.model.jspwrap.StringParamBean",
                  "value": "n/a",
                  "internal": "n/a"
                },
                {
                  "name": "phyletic_indent_map",
                  "prompt": "phyletic_indent_map",
                  "visible": false,
                  "className": "org.gusdb.wdk.model.jspwrap.EnumParamBean",
                  "value": "ARCH",
                  "internal": "ARCH"
                },
                {
                  "name": "phyletic_term_map",
                  "prompt": "phyletic_term_map",
                  "visible": false,
                  "className": "org.gusdb.wdk.model.jspwrap.EnumParamBean",
                  "value": "rnor",
                  "internal": "rnor"
                },
                {
                  "name": "organism",
                  "prompt": "Organism",
                  "visible": true,
                  "className": "org.gusdb.wdk.model.jspwrap.EnumParamBean",
                  "value": "Plasmodium falciparum, Plasmodium vivax, Plasmodium yoelii, Plasmodium berghei, Plasmodium chabaudi, Plasmodium knowlesi",
                  "internal": "Plasmodium falciparum,Plasmodium vivax,Plasmodium yoelii,Plasmodium berghei,Plasmodium chabaudi,Plasmodium knowlesi"
                }
              ]
            }
          },
          "5": {
            "name": "Combine Gene results",
            "customName": "9 INTERSECT 10",
            "id": 11,
            "answerId": 11390220,
            "isCollapsed": false,
            "isUncollapsible": false,
            "dataType": "GeneRecordClasses.GeneRecordClass",
            "displayType": "Gene",
            "shortDisplayType": "Gene",
            "shortName": "Combine Gene results",
            "results": 195,
            "questionName": "InternalQuestions.boolean_question_GeneRecordClasses_GeneRecordClass",
            "displayName": "Combine Gene results",
            "isboolean": true,
            "istransform": false,
            "filtered": false,
            "filterName": "All Results",
            "urlParams": "&value%28bq_left_op_GeneRecordClasses_GeneRecordClass%29=9&value%28bq_right_op_GeneRecordClasses_GeneRecordClass%29=10&value%28bq_operator%29=INTERSECT&value%28use_boolean_filter%29=false",
            "isValid": true,
            "assignedWeight": 0,
            "useweights": true,
            "revisable": true,
            "frontId": 5,
            "params": [
              {
                "name": "bq_left_op_GeneRecordClasses_GeneRecordClass",
                "prompt": "Left Operand",
                "visible": false,
                "className": "org.gusdb.wdk.model.jspwrap.AnswerParamBean",
                "value": "9",
                "internal": "9"
              },
              {
                "name": "bq_right_op_GeneRecordClasses_GeneRecordClass",
                "prompt": "Right Operand",
                "visible": false,
                "className": "org.gusdb.wdk.model.jspwrap.AnswerParamBean",
                "value": "10",
                "internal": "10"
              },
              {
                "name": "bq_operator",
                "prompt": "Operator",
                "visible": true,
                "className": "org.gusdb.wdk.model.jspwrap.StringParamBean",
                "value": "INTERSECT",
                "internal": "INTERSECT"
              },
              {
                "name": "use_boolean_filter",
                "prompt": "Use Expand Filter",
                "visible": true,
                "className": "org.gusdb.wdk.model.jspwrap.StringParamBean",
                "value": "false",
                "internal": "false"
              }
            ],
            "childrenCount": 2,
            "operation": "INTERSECT",
            "step": {
              "name": "SNP Characteristics",
              "customName": "SNPs",
              "id": 10,
              "answerId": 11389990,
              "isCollapsed": false,
              "isUncollapsible": false,
              "dataType": "GeneRecordClasses.GeneRecordClass",
              "displayType": "Gene",
              "shortDisplayType": "Gene",
              "shortName": "SNPs",
              "results": 1944,
              "questionName": "GeneQuestions.GenesBySnps",
              "displayName": "SNP Characteristics",
              "isboolean": false,
              "istransform": false,
              "filtered": false,
              "filterName": "All Results",
              "urlParams": "&value%28occurrences_lower%29=1&array%28snp_stat%29=All+SNPs&array%28snp_strain_m%29=Pf-HB3&value%28snp_density_lower%29=0&value%28dn_ds_ratio_lower%29=0&array%28snp_strain_a%29=Pf-3D7&value%28snp_density_upper%29=10000&value%28occurrences_upper%29=3&array%28organism%29=Plasmodium+falciparum+3D7",
              "isValid": true,
              "assignedWeight": 10,
              "useweights": true,
              "revisable": true,
              "frontId": 1,
              "params": [
                {
                  "name": "organism",
                  "prompt": "Organism",
                  "visible": true,
                  "className": "org.gusdb.wdk.model.jspwrap.EnumParamBean",
                  "value": "Plasmodium falciparum 3D7",
                  "internal": "Plasmodium falciparum 3D7"
                },
                {
                  "name": "snp_strain_a",
                  "prompt": "Reference",
                  "visible": true,
                  "className": "org.gusdb.wdk.model.jspwrap.EnumParamBean",
                  "value": "Pf-3D7",
                  "internal": "Pf-3D7"
                },
                {
                  "name": "snp_strain_m",
                  "prompt": "Comparator",
                  "visible": true,
                  "className": "org.gusdb.wdk.model.jspwrap.EnumParamBean",
                  "value": "Pf-HB3",
                  "internal": "Pf-HB3"
                },
                {
                  "name": "snp_stat",
                  "prompt": "SNP Class",
                  "visible": true,
                  "className": "org.gusdb.wdk.model.jspwrap.EnumParamBean",
                  "value": "All SNPs",
                  "internal": "All SNPs"
                },
                {
                  "name": "occurrences_lower",
                  "prompt": "Number of SNPs of above class >= ",
                  "visible": true,
                  "className": "org.gusdb.wdk.model.jspwrap.StringParamBean",
                  "value": "1",
                  "internal": "1"
                },
                {
                  "name": "occurrences_upper",
                  "prompt": "Number of SNPs of above class <= ",
                  "visible": true,
                  "className": "org.gusdb.wdk.model.jspwrap.StringParamBean",
                  "value": "3",
                  "internal": "3"
                },
                {
                  "name": "dn_ds_ratio_lower",
                  "prompt": "Non-synonymous / synonymous SNP ratio >= ",
                  "visible": true,
                  "className": "org.gusdb.wdk.model.jspwrap.StringParamBean",
                  "value": "0",
                  "internal": "0"
                },
                {
                  "name": "dn_ds_ratio_upper",
                  "prompt": "Non-synonymous / synonymous SNP ratio <= ",
                  "visible": true,
                  "className": "org.gusdb.wdk.model.jspwrap.StringParamBean"
                },
                {
                  "name": "snp_density_lower",
                  "prompt": "SNPs per KB (CDS) >= ",
                  "visible": true,
                  "className": "org.gusdb.wdk.model.jspwrap.StringParamBean",
                  "value": "0",
                  "internal": "0"
                },
                {
                  "name": "snp_density_upper",
                  "prompt": "SNPs per KB (CDS) <= ",
                  "visible": true,
                  "className": "org.gusdb.wdk.model.jspwrap.StringParamBean",
                  "value": "10000",
                  "internal": "10000"
                }
              ]
            }
          },
          "6": {
            "name": "Transform by Orthology",
            "customName": "Orthologs",
            "id": 12,
            "answerId": 11390230,
            "isCollapsed": false,
            "isUncollapsible": false,
            "dataType": "GeneRecordClasses.GeneRecordClass",
            "displayType": "Gene",
            "shortDisplayType": "Gene",
            "shortName": "Orthologs",
            "results": 1383,
            "questionName": "InternalQuestions.GenesByOrthologs",
            "displayName": "Transform by Orthology",
            "isboolean": false,
            "istransform": true,
            "filtered": false,
            "filterName": "All Results",
            "urlParams": "&array%28isSyntenic%29=yes&array%28organism%29=%5BC%5Df48ba82288afd0a82d9a618ee368a2dc&value%28gene_result%29=11",
            "isValid": true,
            "assignedWeight": 0,
            "useweights": true,
            "revisable": true,
            "frontId": 6,
            "params": [
              {
                "name": "gene_result",
                "prompt": "Input Result Set",
                "visible": false,
                "className": "org.gusdb.wdk.model.jspwrap.AnswerParamBean",
                "value": "11",
                "internal": "11"
              },
              {
                "name": "organism",
                "prompt": "Organism",
                "visible": true,
                "className": "org.gusdb.wdk.model.jspwrap.EnumParamBean",
                "value": "Plasmodium falciparum, Plasmodium vivax, Plasmodium yoelii, Plasmodium berghei, Plasmodium chabaudi, Plasmodium knowlesi",
                "internal": "Plasmodium falciparum,Plasmodium vivax,Plasmodium yoelii,Plasmodium berghei,Plasmodium chabaudi,Plasmodium knowlesi"
              },
              {
                "name": "isSyntenic",
                "prompt": "Syntenic Orthologs Only?",
                "visible": true,
                "className": "org.gusdb.wdk.model.jspwrap.EnumParamBean",
                "value": "yes",
                "internal": "yes"
              }
            ],
            "childrenCount": 1
          },
          "length": 6,
          "nonTransformLength": 5
        }
      },
      "length": 1
    }
  };

  exports.testData = testData;
});
