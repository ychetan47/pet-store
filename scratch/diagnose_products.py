import urllib.request
import urllib.error
import json

def test_url(name, url, headers=None):
    if headers is None:
        headers = {}
    print(f"\n--- Testing {name}: {url} ---")
    req = urllib.request.Request(url, headers=headers)
    try:
        with urllib.request.urlopen(req, timeout=5) as resp:
            body = resp.read().decode("utf-8")
            print(f"Status: {resp.status}")
            try:
                data = json.loads(body)
                if isinstance(data, dict):
                    # Print summary
                    data_field = data.get("data")
                    if isinstance(data_field, dict):
                        content = data_field.get("content", [])
                        total = data_field.get("totalElements")
                        print(f"Success: {data.get('success')}, totalElements: {total}, content count: {len(content)}")
                        if content:
                            print(f"First product: id={content[0].get('id')}, name={content[0].get('name')}")
                    else:
                        print(f"Response: {str(data)[:200]}")
                else:
                    print(f"Body: {body[:200]}")
            except Exception:
                print(f"Raw body: {body[:200]}")
    except urllib.error.HTTPError as e:
        print(f"HTTP Error: {e.code} - {e.read().decode('utf-8')[:200]}")
    except Exception as e:
        print(f"Error: {e}")

# 1. Gateway public products
test_url("API Gateway Public Products", "http://localhost:8080/api/products")

# 2. Customer Frontend nginx proxy to products
test_url("Customer Frontend Nginx Proxy", "http://localhost:5173/api/products")

# 3. Admin Login & Products
login_data = json.dumps({"email": "admin@petstore.com", "password": "Admin@123"}).encode("utf-8")
req = urllib.request.Request("http://localhost:8080/api/auth/login", data=login_data, headers={"Content-Type": "application/json"})
try:
    with urllib.request.urlopen(req, timeout=5) as resp:
        login_res = json.loads(resp.read().decode("utf-8"))
        token = login_res["data"]["token"]
        print(f"\nAdmin token retrieved: {token[:20]}...")
        # 4. Gateway Admin Products
        test_url("API Gateway Admin Products", "http://localhost:8080/api/admin/products", headers={"Authorization": f"Bearer {token}"})
        # 5. Admin Frontend nginx proxy
        test_url("Admin Frontend Nginx Proxy", "http://localhost:5174/api/admin/products", headers={"Authorization": f"Bearer {token}"})
except Exception as e:
    print(f"Admin login failed: {e}")

# 6. Test OTel Collector port 4318
test_url("OTel Collector Port 4318", "http://localhost:4318/")
