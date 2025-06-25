import pandas as pd
import sys
from sklearn.model_selection import train_test_split
from sklearn.linear_model import LinearRegression
from sklearn.metrics import root_mean_squared_error, r2_score
import json
import numpy as np

input_csv = sys.argv[1]
target_col = sys.argv[2]
train_percent = float(sys.argv[3]) / 100
output_json = sys.argv[4]

df = pd.read_csv(input_csv)

X = df.drop(columns=[target_col])
y = df[target_col]

X_train, X_test, y_train, y_test = train_test_split(X, y, train_size=train_percent, random_state=42)

model = LinearRegression()
model.fit(X_train, y_train)
y_pred = model.predict(X_test)

metrics = {
    "RMSE": root_mean_squared_error(y_test, y_pred),
    "PRMSE": root_mean_squared_error(y_test, y_pred) / np.mean(y_test),
    "R2_score": r2_score(y_test, y_pred)
}

with open(output_json, "w") as f:
    json.dump(metrics, f, indent=4)
