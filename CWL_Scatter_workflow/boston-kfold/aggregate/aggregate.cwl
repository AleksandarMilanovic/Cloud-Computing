cwlVersion: v1.2
class: CommandLineTool
baseCommand: ["python", "/app/aggregate_metrics.py"]

requirements:
  DockerRequirement:
    dockerPull: aleksandarmilanovic/aggregate:latest

inputs:
  metrics_files:
    type: File[]    
    inputBinding:
      position: 1
  output_json:
    type: string
    inputBinding:
      position: 2

outputs:
  summary:
    type: File
    outputBinding:
      glob: $(inputs.output_json)
