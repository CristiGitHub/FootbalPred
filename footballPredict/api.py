import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import LabelEncoder, StandardScaler
from sklearn.linear_model import LogisticRegression
from sklearn.svm import SVC
import joblib
from tensorflow import keras
from tensorflow.keras import layers
from fastapi import FastAPI, File, UploadFile

app = FastAPI()



def update_label_encoder(encoder, values, filename):
    known_classes = set(encoder.classes_)
    new_values = set(values) - known_classes

    if new_values:
        updated_classes = list(known_classes) + list(new_values)
        encoder.fit(updated_classes)
        joblib.dump(encoder, filename)

    return encoder

# Function for future match prediction
def predict_match(home_team, away_team, championship, h_bet=None, x_bet=None, a_bet=None):
    # Load encoders and scaler
    team_encoder = joblib.load("team_encoder.pkl")
    championship_encoder = joblib.load("championship_encoder.pkl")
    scaler = joblib.load("scaler.pkl")
    label_encoder = joblib.load("label_encoder.pkl")

    # Update encoders dynamically if new labels appear
    team_encoder = update_label_encoder(team_encoder, [home_team, away_team], "team_encoder.pkl")
    championship_encoder = update_label_encoder(championship_encoder, [championship], "championship_encoder.pkl")

    # Encode inputs
    home_team_encoded = team_encoder.transform([home_team])[0]
    away_team_encoded = team_encoder.transform([away_team])[0]
    championship_encoded = championship_encoder.transform([championship])[0]

    # Create input dataframe with only selected features
    match_data = pd.DataFrame([[home_team_encoded, away_team_encoded, championship_encoded, h_bet, x_bet, a_bet]],
                              columns=["Home", "Away", "League", "H_BET", "X_BET", "A_BET"])
    match_data = match_data.fillna(0)  # Fill missing odds with 0
    final_match_scaled = scaler.transform(match_data)

    # Load models
    log_reg = joblib.load("logistic_regression.h5")
    svm_model = joblib.load("svm_model.h5")
    ann_model = keras.models.load_model("ann_model.h5")

    # Get predictions
    log_pred = log_reg.predict_proba(final_match_scaled)
    svm_pred = svm_model.predict_proba(final_match_scaled)
    ann_pred = ann_model.predict(final_match_scaled)

    # Average predictions (soft voting)
    final_pred = (log_pred + svm_pred + ann_pred) / 3

    # Check if predictions are too close
    if np.max(final_pred) - np.min(final_pred) < 0.1:
        return "No Bet"

    result = np.argmax(final_pred)
    return label_encoder.inverse_transform([result])[0]


@app.get("/predict/")
async def classify_image(home_team:str, away_team:str, championship:str, h_bet=None, x_bet=None, a_bet=None) -> str:
    prediction = predict_match(home_team,away_team,championship,h_bet,x_bet,a_bet)
    if prediction:
        return prediction
    else:
        return 'No Bet'

if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="127.0.0.1", port=8000)


# print(predict_match("Real Madryt","Atl. Madryt","laliga",1.1,5.0,4.5))
