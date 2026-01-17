import sys, json, warnings, logging
import pandas as pd
from prophet import Prophet

warnings.filterwarnings("ignore")
logging.getLogger('prophet').setLevel(logging.ERROR)
logging.getLogger('cmdstanpy').setLevel(logging.ERROR)

coin = sys.argv[1]  # "BTC"
timestamps = [int(t) for t in sys.argv[2].split(",")]
prices = [float(p) for p in sys.argv[3].split(",")]
df = pd.DataFrame({
    'ds': pd.to_datetime(timestamps, unit='ms'),
    'y': prices
})

model = Prophet()
model.fit(df)

future = model.make_future_dataframe(periods=24, freq='h')
forecast = model.predict(future)

predicted_times = [int(pd.Timestamp(t).timestamp() * 1000) for t in forecast['ds'].tail(24)]
predicted_prices = forecast['yhat'].tail(24).tolist()

# **ONLY print JSON**
print(json.dumps({
    "times": predicted_times,
    "prices": predicted_prices
}))
