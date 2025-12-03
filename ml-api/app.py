from flask import Flask, request, jsonify
from flask_cors import CORS
import joblib
import numpy as np
import pandas as pd

app = Flask(__name__)
CORS(app)

# Cargar modelo y threshold
model = joblib.load('best_model.pkl')
threshold = joblib.load('best_threshold.pkl')

# Orden de features (debe coincidir con el entrenamiento)
FEATURE_ORDER = [
    'leadTime',
    'avgPricePerRoom',
    'noOfSpecialRequests',
    'arrivalDate',
    'arrivalMonth',
    'noOfWeekNights',
    'noOfWeekendNights',
    'previousCancellations',
    'previousBookingsNotCanceled',
    'isRepeatedGuest'
]


def get_risk_level(probability):
    """Determina el nivel de riesgo según la probabilidad"""
    if probability < 0.3:
        return 'LOW'
    elif probability < 0.6:
        return 'MEDIUM'
    return 'HIGH'


def extract_features(booking):
    """Extrae features de un booking en el orden correcto"""
    return [
        booking['leadTime'],
        float(booking['avgPricePerRoom']),
        booking['noOfSpecialRequests'],
        booking['arrivalDate'],
        booking['arrivalMonth'],
        booking['noOfWeekNights'],
        booking['noOfWeekendNights'],
        booking['previousCancellations'],
        booking['previousBookingsNotCanceled'],
        1 if booking['isRepeatedGuest'] else 0
    ]


@app.route('/health', methods=['GET'])
def health():
    """Endpoint de salud para verificar que la API está funcionando"""
    return jsonify({
        'status': 'healthy',
        'model': 'loaded',
        'threshold': float(threshold),
        'features': FEATURE_ORDER
    })


@app.route('/predict', methods=['POST'])
def predict():
    """Predicción individual - recibe un PredictionFeatureDTO"""
    try:
        data = request.get_json()
        booking_id = data.get('bookingId')
        features = pd.DataFrame([extract_features(data)], columns=FEATURE_ORDER)
        
        probability = model.predict_proba(features)[0][1]
        will_cancel = probability >= threshold
        
        return jsonify({
            'bookingId': booking_id,
            'cancellationProbability': round(float(probability), 4),
            'willCancel': bool(will_cancel),
            'riskLevel': get_risk_level(probability)
        })
        
    except KeyError as e:
        return jsonify({'error': f'Campo faltante: {str(e)}'}), 400
    except Exception as e:
        return jsonify({'error': str(e)}), 500


@app.route('/predict/batch', methods=['POST'])
def predict_batch():
    """Predicción batch - recibe BatchPredictionRequestDTO"""
    try:
        data = request.get_json()
        bookings = data.get('bookings', [])
        
        if not bookings:
            return jsonify([])
        
        # Extraer IDs y features
        booking_ids = [b.get('bookingId') for b in bookings]
        features_matrix = pd.DataFrame(
            [extract_features(b) for b in bookings],
            columns=FEATURE_ORDER
        )
        
        # Predicción batch
        probabilities = model.predict_proba(features_matrix)[:, 1]
        
        # Construir respuesta con bookingId incluido
        results = []
        for booking_id, prob in zip(booking_ids, probabilities):
            will_cancel = prob >= threshold
            results.append({
                'bookingId': booking_id,
                'cancellationProbability': round(float(prob), 4),
                'willCancel': bool(will_cancel),
                'riskLevel': get_risk_level(prob)
            })
        
        return jsonify(results)
        
    except KeyError as e:
        return jsonify({'error': f'Campo faltante: {str(e)}'}), 400
    except Exception as e:
        return jsonify({'error': str(e)}), 500


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)