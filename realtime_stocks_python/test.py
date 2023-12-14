import requests

import json
# replace the "demo" apikey below with your own key from https://www.alphavantage.co/support/#api-key
url = 'https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol=AAPL&interval=5min&apikey=7OZOVCFWO31662Q8'
r = requests.get(url)
import pdb
pdb.set_trace()
data = r.json()

json_data = json.dumps(data, indent=3)

with open("apple.json", "w") as outfile:
    outfile.write(json_data)

print(data)