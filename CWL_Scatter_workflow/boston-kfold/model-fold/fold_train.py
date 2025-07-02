import pandas as pd
import numpy as np
import sys
import json
from sklearn.linear_model import LinearRegression
from sklearn.metrics import mean_squared_error, r2_score

csv_path = sys.argv[1]
target_col = sys.argv[2]
k = int(sys.argv[3])
fold_index = int(sys.argv[4])
output_path = sys.argv[5]

df = pd.read_csv(csv_path)
df = df.fillna(df.mean(numeric_only=True))
folds = np.array_split(df.sample(frac=1, random_state=42), k)
test = folds[fold_index]
train = pd.concat([f for i, f in enumerate(folds) if i != fold_index])

X_train = train.drop(columns=[target_col])
y_train = train[target_col]
X_test = test.drop(columns=[target_col])
y_test = test[target_col]

model = LinearRegression()
model.fit(X_train, y_train)
y_pred = model.predict(X_test)

rmse = np.sqrt(mean_squared_error(y_test, y_pred))
prmse = rmse / y_test.mean()
r2 = r2_score(y_test, y_pred)

with open(output_path, "w") as f:
    json.dump({"fold": fold_index, "RMSE": rmse, "PRMSE": prmse, "R2_score": r2}, f, indent=4)
