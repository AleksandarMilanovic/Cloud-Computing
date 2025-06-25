import pandas as pd
import sys

input_csv = sys.argv[1]
target_col = sys.argv[2]
output_csv = sys.argv[3]

df = pd.read_csv(input_csv)

# Popunjavanje NaN vrednosti
df.fillna(df.mean(), inplace=True)

# Uklanjanje outliera po IQR pravilu
# Outlieri su one vrednosti koje su:
#       Manje od Q1 - 1.5 * IQR
#       Veće od Q3 + 1.5 * IQR

Q1 = df.quantile(0.25)
Q3 = df.quantile(0.75)
IQR = Q3 - Q1
df = df[~((df < (Q1 - 1.5 * IQR)) | (df > (Q3 + 1.5 * IQR))).any(axis=1)]

df.to_csv(output_csv, index=False)
