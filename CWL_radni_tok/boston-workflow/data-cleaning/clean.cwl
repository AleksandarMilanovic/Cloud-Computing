cwlVersion: v1.2
class: CommandLineTool
baseCommand: ["python", "/app/clean_data.py"]
requirements:
  DockerRequirement:
    dockerPull: aleksandarmilanovic/data-cleaning:latest
inputs:
  input_csv:
    type: File
    inputBinding:
      position: 1
  target_col:
    type: string
    inputBinding:
      position: 2
  output_csv:
    type: string
    inputBinding:
      position: 3
outputs:
  cleaned_csv:
    type: File
    outputBinding:
      glob: $(inputs.output_csv)
