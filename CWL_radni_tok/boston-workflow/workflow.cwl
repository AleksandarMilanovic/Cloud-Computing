cwlVersion: v1.2
class: Workflow

inputs:
  input_csv: File
  target_col: string
  train_percent: float

outputs:
  model_metrics:
    type: File
    outputSource: train_step/metrics

steps:
  clean_step:
    run: data-cleaning/clean.cwl
    in:
      input_csv: input_csv
      target_col: target_col
      output_csv: {default: "cleaned.csv"}
    out: [cleaned_csv]

  train_step:
    run: model-training/train.cwl
    in:
      input_csv: clean_step/cleaned_csv
      target_col: target_col
      train_percent: train_percent
      output_json: {default: "metrics.json"}
    out: [metrics]
