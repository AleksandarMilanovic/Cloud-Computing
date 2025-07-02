cwlVersion: v1.2
class: Workflow

requirements:
  InlineJavascriptRequirement: {}
  ScatterFeatureRequirement: {}
  StepInputExpressionRequirement: {}

inputs:
  csv_path: File
  target_col: string
  k: int

outputs:
  final_summary:
    type: File
    outputSource: aggregate_step/summary

steps:
  generate_indices:
    run:
      class: ExpressionTool
      requirements:
        InlineJavascriptRequirement: {}
      inputs:
        k: int
      outputs:
        indices:
          type: int[]
      expression: |
        ${
          var arr = [];
          for (var i = 0; i < inputs.k; i++) {
            arr.push(i);
          }
          return { indices: arr };
        }
    in:
      k: k
    out: [indices]

  scatter_folds:
    run: model-fold/fold_train.cwl
    in:
      csv_path: csv_path
      target_col: target_col
      k: k
      fold_index: generate_indices/indices
      output_json:
        valueFrom: $( 'fold' + inputs.fold_index + '.json' )
    out: [metrics]
    scatter: fold_index
    scatterMethod: dotproduct

  aggregate_step:
    run: aggregate/aggregate.cwl
    in:
      metrics_files: scatter_folds/metrics
      output_json: { default: "summary.json" }
    out: [summary]
