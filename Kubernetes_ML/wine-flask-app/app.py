from flask import Flask, request, jsonify
from sklearn.model_selection import train_test_split
from sklearn.neural_network import MLPClassifier
from sklearn.metrics import precision_score, recall_score, f1_score
import pandas as pd
import joblib
import os

app = Flask(__name__)

MODEL_PATH = "data/wine_model.pkl"
CSV_PATH = "data/wine.csv"

@app.route("/", methods=["GET"])
def home():
    return "Wine Classification API is running. Use POST /train and POST /predict."


@app.route("/train", methods=["POST"])
def train():
    df = pd.read_csv(CSV_PATH)
    df["type"] = df["type"].map({"white": 0, "red": 1})

    X = df.drop(columns=["quality"])
    y = df["quality"]

    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)
    clf = MLPClassifier(random_state=42, max_iter=500)
    clf.fit(X_train, y_train)

    y_pred = clf.predict(X_test)

    joblib.dump(clf, MODEL_PATH)

    return jsonify({
        "precision": precision_score(y_test, y_pred, average="weighted", zero_division=1),
        "recall": recall_score(y_test, y_pred, average="weighted", zero_division=1),
        "f1_score": f1_score(y_test, y_pred, average="weighted", zero_division=1)
    })

# @app.route("/predict", methods=["POST"])
# def predict():
#     data = request.get_json()
#     model = joblib.load(MODEL_PATH)
#     pred = model.predict([data["features"]])
#     return jsonify({"prediction": int(pred[0])})

@app.route("/predict", methods=["POST"])
def predict():
    data = request.get_json()

    df = pd.DataFrame([data])

    df["type"] = df["type"].map({"white": 0, "red": 1})

    model = joblib.load(MODEL_PATH) #ucitavamo vec istrenirani model sa svim parametrima
    pred = model.predict(df)

    return jsonify({"prediction": int(pred[0])})


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)
