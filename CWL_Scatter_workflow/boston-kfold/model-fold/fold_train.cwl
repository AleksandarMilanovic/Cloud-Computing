cwlVersion: v1.2
class: CommandLineTool
baseCommand: python
arguments:
  - /app/fold_train.py
inputs:
  csv_path:
    type: File
    inputBinding:
      position: 1
  target_col:
    type: string
    inputBinding:
      position: 2
  k:
    type: int
    inputBinding:
      position: 3
  fold_index:
    type: int
    inputBinding:
      position: 4
  output_json:
    type: string
    inputBinding:
      position: 5

outputs:
  metrics:
    type: File
    outputBinding:
      glob: $(inputs.output_json)

requirements:
  DockerRequirement:
    dockerPull: aleksandarmilanovic/fold-train:latest
