import requests
import json

base_url = "http://localhost:8081/api"

def login():
    url = f"{base_url}/auth/login"
    data = {
        "username": "admin",
        "password": "123456"
    }
    try:
        response = requests.post(url, json=data)
        if response.status_code == 200:
            res = response.json()
            if res['code'] == 200:
                print("Login successful")
                return res['data']['token']
            else:
                print(f"Login failed: {res['message']}")
        else:
            print(f"Login error: {response.status_code} {response.text}")
    except Exception as e:
        print(f"Login exception: {e}")
    return None

def get_products(token):
    url = f"{base_url}/admin/shop/list"
    headers = {
        "Authorization": f"Bearer {token}"
    }
    params = {
        "page": 1,
        "size": 100
    }
    try:
        response = requests.get(url, headers=headers, params=params)
        print(f"Status: {response.status_code}")
        print(f"Response: {response.text}")
    except Exception as e:
        print(f"Get products exception: {e}")

if __name__ == "__main__":
    token = login()
    if token:
        get_products(token)
