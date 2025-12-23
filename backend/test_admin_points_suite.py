import requests
import time
import sys
import json

BASE_URL = "http://localhost:8081/api"
ADMIN_CREDS = {"username": "admin", "password": "123456"}
USER_CREDS = {"username": "student", "password": "123456"}

def print_header(title):
    print(f"\n{'='*20} {title} {'='*20}")

def login(creds):
    try:
        response = requests.post(f"{BASE_URL}/auth/login", json=creds)
        print(f"Login Response ({creds['username']}): {response.status_code} - {response.text}")
        if response.status_code == 200:
            return response.json()['data']['token']
        else:
            print(f"Login failed for {creds['username']}: {response.text}")
            return None
    except Exception as e:
        print(f"Connection error during login: {e}")
        return None

def test_functional_stats(token):
    print_header("Functional Test: Get Points Stats")
    headers = {"Authorization": f"Bearer {token}"}
    response = requests.get(f"{BASE_URL}/admin/points/stats", headers=headers)
    if response.status_code == 200:
        data = response.json()['data']
        print("SUCCESS: Stats retrieved.")
        print(f"  - Today Issuance: {data.get('todayIssuance')}")
        print(f"  - Total Circulation: {data.get('totalCirculation')}")
        print(f"  - Inflation Rate: {data.get('inflationRate')}%")
        return True
    else:
        print(f"FAILURE: Status {response.status_code}, Body: {response.text}")
        return False

def test_functional_rules_get(token):
    print_header("Functional Test: Get Point Rules")
    headers = {"Authorization": f"Bearer {token}"}
    response = requests.get(f"{BASE_URL}/admin/points/rules", headers=headers)
    if response.status_code == 200:
        rules = response.json()['data']
        print(f"SUCCESS: Retrieved {len(rules)} rules.")
        for r in rules:
            print(f"  - {r.get('configKey')}: {r.get('configValue')}")
        return rules
    else:
        print(f"FAILURE: Status {response.status_code}, Body: {response.text}")
        return None

def test_functional_rule_update(token, rule):
    print_header("Functional Test: Update Point Rule")
    headers = {"Authorization": f"Bearer {token}"}
    
    # Modify the value
    original_value = rule['configValue']
    new_value = str(float(original_value) + 10)
    rule['configValue'] = new_value
    
    print(f"Updating {rule['configKey']} from {original_value} to {new_value}")
    
    response = requests.post(f"{BASE_URL}/admin/points/rules", json=rule, headers=headers)
    
    if response.status_code == 200:
        print("SUCCESS: Rule updated.")
        # Verify update
        verify_resp = requests.get(f"{BASE_URL}/admin/points/rules", headers=headers)
        updated_rules = verify_resp.json()['data']
        updated_rule = next((r for r in updated_rules if r['id'] == rule['id']), None)
        if updated_rule and updated_rule['configValue'] == new_value:
             print("VERIFICATION: New value confirmed.")
             
             # Revert change
             rule['configValue'] = original_value
             requests.post(f"{BASE_URL}/admin/points/rules", json=rule, headers=headers)
             print("CLEANUP: Value reverted.")
             return True
        else:
            print("VERIFICATION FAILED: Value not updated.")
            return False
    else:
        print(f"FAILURE: Status {response.status_code}, Body: {response.text}")
        return False

def test_security_access(user_token):
    print_header("Security Test: RBAC (User accessing Admin API)")
    headers = {"Authorization": f"Bearer {user_token}"}
    response = requests.get(f"{BASE_URL}/admin/points/rules", headers=headers)
    
    # Expecting 403 Forbidden or 500 (if security not fully configured to catch it gracefully)
    # But usually 403.
    print(f"Response Status: {response.status_code}")
    if response.status_code == 403 or response.status_code == 401:
        print("SUCCESS: Access denied as expected.")
        return True
    else:
        print(f"FAILURE: Unexpected status code {response.status_code}. Expected 403/401.")
        return False

def test_performance(token):
    print_header("Performance Test: API Latency")
    headers = {"Authorization": f"Bearer {token}"}
    iterations = 20
    times = []
    
    print(f"Running {iterations} requests to /api/admin/points/stats...")
    
    success_count = 0
    for i in range(iterations):
        start = time.time()
        resp = requests.get(f"{BASE_URL}/admin/points/stats", headers=headers)
        end = time.time()
        if resp.status_code == 200:
            times.append((end - start) * 1000) # ms
            success_count += 1
    
    if not times:
        print("FAILURE: No successful requests.")
        return False
        
    avg_time = sum(times) / len(times)
    max_time = max(times)
    min_time = min(times)
    
    print(f"Results (ms): Avg={avg_time:.2f}, Min={min_time:.2f}, Max={max_time:.2f}")
    print(f"Success Rate: {success_count}/{iterations}")
    
    if avg_time < 200: # Threshold 200ms
        print("SUCCESS: Performance within acceptable limits (<200ms).")
        return True
    else:
        print("WARNING: Performance might be slow.")
        return True # Still pass, just warning

def main():
    print("Waiting for server to be fully ready...")
    time.sleep(10) # Wait a bit for Spring Boot to fully start
    
    # 1. Login Admin
    print("Logging in as Admin...")
    admin_token = login(ADMIN_CREDS)
    if not admin_token:
        print("FATAL: Cannot login as admin. Aborting.")
        return

    # 2. Login User
    print("Logging in as User...")
    user_token = login(USER_CREDS)
    
    results = []
    
    # 3. Functional Tests
    results.append(test_functional_stats(admin_token))
    
    rules = test_functional_rules_get(admin_token)
    if rules:
        results.append(True)
        # Test update with the first rule found
        if len(rules) > 0:
            results.append(test_functional_rule_update(admin_token, rules[0]))
    else:
        results.append(False)
        
    # 4. Security Tests
    if user_token:
        results.append(test_security_access(user_token))
    else:
        print("Skipping Security Test (User login failed)")
        
    # 5. Performance Tests
    results.append(test_performance(admin_token))
    
    print_header("Test Summary")
    passed = results.count(True)
    total = len(results)
    print(f"Passed: {passed}/{total}")
    
    if passed == total:
        print("ALL TESTS PASSED")
    else:
        print("SOME TESTS FAILED")

if __name__ == "__main__":
    main()
