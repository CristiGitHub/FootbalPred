import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import LabelEncoder, StandardScaler
from sklearn.linear_model import LogisticRegression
from sklearn.svm import SVC
import joblib
from tensorflow import keras
from tensorflow.keras import layers

# Load dataset
df = pd.read_csv("full_data.csv")

# Verify column names
print("Dataset Columns:", df.columns)

# Ensure required columns exist
required_columns = ["Home", "Away", "League", "H_BET", "X_BET", "A_BET"]
for col in required_columns:
    if col not in df.columns:
        raise KeyError(f"Missing column: {col}")

# Selecting relevant features (only what we have for future matches)
selected_features = ["Home", "Away", "League", "H_BET", "X_BET", "A_BET"]

# Target variable
target_column = "WIN"

# Drop rows with missing target values
df = df.dropna(subset=[target_column])

# Encode target variable (Home Win = 0, Draw = 1, Away Win = 2)
label_encoder = LabelEncoder()
df[target_column] = label_encoder.fit_transform(df[target_column])
joblib.dump(label_encoder, "label_encoder.pkl")  # Save target encoder

# Keep only relevant columns and drop rows with missing values in selected features
df_selected = df[selected_features + [target_column]].dropna()


# Encode categorical features (teams & championship)
def update_label_encoder(encoder, values, filename):
    known_classes = set(encoder.classes_)
    new_values = set(values) - known_classes

    if new_values:
        updated_classes = list(known_classes) + list(new_values)
        encoder.fit(updated_classes)
        joblib.dump(encoder, filename)

    return encoder


try:
    team_encoder = joblib.load("team_encoder.pkl")
except FileNotFoundError:
    team_encoder = LabelEncoder()
    team_encoder.fit(df_selected["Home"].astype(str).tolist() + df_selected["Away"].astype(str).tolist())

team_encoder = update_label_encoder(team_encoder,
                                    df_selected["Home"].astype(str).tolist() + df_selected["Away"].astype(str).tolist(),
                                    "team_encoder.pkl")
df_selected["Home"] = team_encoder.transform(df_selected["Home"].astype(str))
df_selected["Away"] = team_encoder.transform(df_selected["Away"].astype(str))

try:
    championship_encoder = joblib.load("championship_encoder.pkl")
except FileNotFoundError:
    championship_encoder = LabelEncoder()
    championship_encoder.fit(df_selected["League"].astype(str).tolist())

championship_encoder = update_label_encoder(championship_encoder, df_selected["League"].astype(str).tolist(),
                                            "championship_encoder.pkl")
df_selected["League"] = championship_encoder.transform(df_selected["League"].astype(str))

# Save encoders
joblib.dump(team_encoder, "team_encoder.pkl")
joblib.dump(championship_encoder, "championship_encoder.pkl")

# Split data into features and target
X = df_selected.drop(columns=[target_column])
y = df_selected[target_column]

# Standardize numerical features
scaler = StandardScaler()
X_scaled = scaler.fit_transform(X)
joblib.dump(scaler, "scaler.pkl")  # Save scaler

# Split into train and test sets
X_train, X_test, y_train, y_test = train_test_split(X_scaled, y, test_size=0.2, random_state=42, stratify=y)

# Logistic Regression Model
log_reg = LogisticRegression(max_iter=1000, random_state=42)
log_reg.fit(X_train, y_train)
joblib.dump(log_reg, "logistic_regression.h5")

# Support Vector Machine (SVM) Model
svm_model = SVC(probability=True, random_state=42)
svm_model.fit(X_train, y_train)
joblib.dump(svm_model, "svm_model.h5")

# Artificial Neural Network (ANN) Model
ann_model = keras.Sequential([
    layers.Dense(64, activation='relu', input_shape=(X_train.shape[1],)),
    layers.Dense(32, activation='relu'),
    layers.Dense(3, activation='softmax')  # 3 output classes: Home Win, Draw, Away Win
])

# Compile and train ANN
ann_model.compile(optimizer='adam', loss='sparse_categorical_crossentropy', metrics=['accuracy'])
ann_model.fit(X_train, y_train, epochs=20, batch_size=32, validation_data=(X_test, y_test), verbose=1)

# Save ANN model
ann_model.save("ann_model.h5")



