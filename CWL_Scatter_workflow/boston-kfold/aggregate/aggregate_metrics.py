import pandas as pd
import sys
import json

# Prvih N argumenata su putanje do fold_X.json fajlova
# Poslednji argument je output file

input_files = sys.argv[1:-1]
output_json = sys.argv[-1]

all_metrics = []

for file_path in input_files:
    with open(file_path, 'r') as f:
        metrics = json.load(f)
        all_metrics.append(metrics)

df = pd.DataFrame(all_metrics)

# Racunamo prosek za RMSE, PRMSE i R2_score iz svih dobijenih foldova
summary = {
    "RMSE_mean": df["RMSE"].mean(),
    "PRMSE_mean": df["PRMSE"].mean(),
    "R2_score_mean": df["R2_score"].mean()
}

with open(output_json, "w") as f:
    json.dump(summary, f, indent=4)
