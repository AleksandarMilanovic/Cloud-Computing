cwlVersion: v1.2
class: CommandLineTool
baseCommand: ["python", "/app/train_model.py"]
requirements:
  DockerRequirement:
    dockerPull: aleksandarmilanovic/model-training:latest
inputs:
  input_csv:
    type: File
    inputBinding:
      position: 1
  target_col:
    type: string
    inputBinding:
      position: 2
  train_percent:
    type: float
    inputBinding:
      position: 3
  output_json:
    type: string
    inputBinding:
      position: 4
outputs:
  metrics:
    type: File
    outputBinding:
      glob: $(inputs.output_json)
