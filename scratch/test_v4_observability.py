import urllib.request
import urllib.error
import json
import time
import uuid
import sys

BASE_URL = "http://localhost:8080"
PROMETHEUS_URL = "http://localhost:9090"
GRAFANA_URL = "http://localhost:3000"
ELASTICSEARCH_URL = "http://localhost:9200"

def http_request(url, method="GET", data=None, headers=None):
    if headers is None:
        headers = {}
    encoded_data = None
    if data is not None:
        encoded_data = json.dumps(data).encode("utf-8")
        headers["Content-Type"] = "application/json"

    req = urllib.request.Request(url, data=encoded_data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=10) as resp:
            body = resp.read().decode("utf-8")
            resp_headers = dict(resp.headers)
            try:
                json_data = json.loads(body)
            except Exception:
                json_data = body
            return resp.status, json_data, resp_headers
    except urllib.error.HTTPError as e:
        body = e.read().decode("utf-8")
        try:
            json_data = json.loads(body)
        except Exception:
            json_data = body
        return e.code, json_data, dict(e.headers)
    except Exception as e:
        return 0, str(e), {}

def test_service_health():
    print("\n--- 1. Testing Actuator Health & Probes ---")
    services = {
        "api-gateway": 8080,
        "user-service": 8081,
        "catalog-service": 8082,
        "order-service": 8083,
        "inventory-service": 8084,
        "notification-service": 8085
    }
    all_ok = True
    for s_name, port in services.items():
        url = f"http://localhost:{port}/actuator/health"
        status, data, _ = http_request(url)
        print(f"[{s_name}] Health: {data.get('status') if isinstance(data, dict) else data} (HTTP {status})")
        if status != 200 or not isinstance(data, dict) or data.get("status") != "UP":
            all_ok = False
    return all_ok

def test_prometheus_scrape_targets():
    print("\n--- 2. Testing Prometheus Scrape Targets ---")
    status, data, _ = http_request(f"{PROMETHEUS_URL}/api/v1/targets")
    if status != 200 or not isinstance(data, dict):
        print(f"Failed to query Prometheus: {data}")
        return False
    targets = data.get("data", {}).get("activeTargets", [])
    print(f"Found {len(targets)} active targets in Prometheus:")
    all_up = True
    for t in targets:
        job = t.get("labels", {}).get("job")
        health = t.get("health")
        print(f"  - Job: {job:<22} -> Health: {health}")
        if health != "up":
            all_up = False
    return all_up and len(targets) >= 6

def test_end_to_end_flow_and_metrics():
    print("\n--- 3. Testing E2E Order Flow & Trace/Metrics Generation ---")
    test_id = str(uuid.uuid4())[:8]
    email = f"v4_test_{test_id}@example.com"
    password = "Password@123"

    # 1. Register User
    reg_payload = {
        "name": "V4 Test User",
        "email": email,
        "password": password,
        "phone": "9876543210"
    }
    print(f"Registering user: {email}")
    status, reg_res, _ = http_request(f"{BASE_URL}/api/auth/register", method="POST", data=reg_payload)
    if status not in (200, 201):
        print(f"Registration failed: {reg_res}")
        return False

    token = reg_res["data"]["token"]
    user_id = reg_res["data"].get("id") or reg_res["data"].get("user", {}).get("id")
    headers = {"Authorization": f"Bearer {token}"}

    # 2. Add Address
    addr_payload = {
        "name": "V4 Tester",
        "phone": "9876543210",
        "addressLine1": "123 Observability Lane",
        "city": "Bengaluru",
        "state": "Karnataka",
        "pincode": "560001",
        "isDefault": True
    }
    status, addr_res, _ = http_request(f"{BASE_URL}/api/addresses", method="POST", data=addr_payload, headers=headers)
    if status not in (200, 201):
        print(f"Address creation failed: {addr_res}")
        return False
    address_id = addr_res["data"]["id"]
    print(f"Address added: ID {address_id}")

    # 3. Fetch valid product and add to cart
    p_status, p_res, _ = http_request(f"{BASE_URL}/api/products?page=0&size=5")
    prod_id = 1
    if p_status == 200 and isinstance(p_res, dict) and "data" in p_res:
        items = p_res["data"].get("content", [])
        for it in items:
            if it.get("stockQuantity", 0) > 0:
                prod_id = it.get("id")
                break
    print(f"Using product ID {prod_id} for order test")

    cart_payload = {"productId": prod_id, "quantity": 1}
    status, cart_res, _ = http_request(f"{BASE_URL}/api/cart/items", method="POST", data=cart_payload, headers=headers)
    if status not in (200, 201):
        print(f"Cart add failed: {cart_res}")
        return False
    print(f"Added product {prod_id} to cart: HTTP {status}")

    # 4. Place COD Order
    checkout_payload = {
        "addressId": address_id,
        "paymentMethod": "COD"
    }
    status, order_res, resp_headers = http_request(f"{BASE_URL}/api/orders", method="POST", data=checkout_payload, headers=headers)
    if status not in (200, 201):
        print(f"Order placement failed: {order_res}")
        return False

    order_data = order_res["data"]
    order_id = order_data["id"]
    corr_id = resp_headers.get("X-Correlation-ID", "N/A")
    print(f"Order placed successfully! ID: {order_id}, Initial Status: {order_data['orderStatus']}, Correlation-ID: {corr_id}")

    # 5. Wait for Kafka Outbox -> Inventory Reservation -> Order Confirmation
    print("Waiting 5 seconds for Kafka event roundtrip and outbox publisher...")
    time.sleep(5)

    status, order_check, _ = http_request(f"{BASE_URL}/api/orders/{order_id}", headers=headers)
    final_status = order_check["data"]["orderStatus"]
    print(f"Order #{order_id} final status: {final_status}")
    return final_status == "CONFIRMED"

def test_elasticsearch_logs():
    print("\n--- 4. Testing Centralized Logging in Elasticsearch ---")
    # Check indices
    status, ind_res, _ = http_request(f"{ELASTICSEARCH_URL}/_cat/indices?format=json")
    if status != 200:
        print(f"Elasticsearch _cat/indices failed: {ind_res}")
        return False

    indices = [i.get("index") for i in ind_res] if isinstance(ind_res, list) else []
    print("Indices in Elasticsearch:", indices)

    # Search for recent logs
    status, search_res, _ = http_request(f"{ELASTICSEARCH_URL}/petstore-logs-*/_search?size=5&sort=@timestamp:desc")
    if status != 200:
        print(f"Elasticsearch search failed: {search_res}")
        return False

    hits = search_res.get("hits", {}).get("hits", [])
    print(f"Found {len(hits)} recent log documents in Elasticsearch.")
    if hits:
        sample = hits[0]["_source"]
        print("Sample log entry fields:")
        print(f"  - service:       {sample.get('service')}")
        print(f"  - traceId:       {sample.get('traceId')}")
        print(f"  - spanId:        {sample.get('spanId')}")
        print(f"  - correlationId: {sample.get('correlationId')}")
        print(f"  - message:       {sample.get('message')}")
        return True
    return False

def test_rate_limiting():
    print("\n--- 5. Testing API Gateway Rate Limiting ---")
    hit_429 = False
    for i in range(25):
        status, res, headers = http_request(
            f"{BASE_URL}/api/auth/login",
            method="POST",
            data={"email": "nobody@example.com", "password": "wrong"}
        )
        if status == 429:
            print(f"Rate limit triggered on request #{i+1}: HTTP {status} ({res})")
            print(f"Retry-After header: {headers.get('Retry-After')}")
            hit_429 = True
            break
    return hit_429

def test_grafana():
    print("\n--- 6. Testing Grafana Health & Dashboards ---")
    status, health, _ = http_request(f"{GRAFANA_URL}/api/health")
    print(f"Grafana health: {health} (HTTP {status})")

    # Basic auth for Grafana
    import base64
    auth_str = base64.b64encode(b"admin:admin").decode("utf-8")
    status, dashboards, _ = http_request(
        f"{GRAFANA_URL}/api/search",
        headers={"Authorization": f"Basic {auth_str}"}
    )
    if status != 200 or not isinstance(dashboards, list):
        print(f"Grafana dashboard query returned: {dashboards}")
        return False

    print(f"Provisioned Grafana Dashboards ({len(dashboards)}):")
    for d in dashboards:
        print(f"  - {d.get('title')} (UID: {d.get('uid')})")
    return len(dashboards) >= 4

if __name__ == "__main__":
    print("Beginning V4 Observability Verification...")
    ok1 = test_service_health()
    ok2 = test_prometheus_scrape_targets()
    ok3 = test_end_to_end_flow_and_metrics()
    time.sleep(3) # Wait for logs to flush to logstash/elasticsearch
    ok4 = test_elasticsearch_logs()
    ok5 = test_rate_limiting()
    ok6 = test_grafana()

    print("\n================== SUMMARY ==================")
    print(f"1. Service Health & Probes: {'PASS' if ok1 else 'FAIL'}")
    print(f"2. Prometheus Scrapes:      {'PASS' if ok2 else 'FAIL'}")
    print(f"3. E2E Order & Outbox Flow: {'PASS' if ok3 else 'FAIL'}")
    print(f"4. Elasticsearch Logs:      {'PASS' if ok4 else 'FAIL'}")
    print(f"5. Gateway Rate Limiting:   {'PASS' if ok5 else 'FAIL'}")
    print(f"6. Grafana Dashboards:      {'PASS' if ok6 else 'FAIL'}")

    all_passed = ok1 and ok2 and ok3 and ok4 and ok5 and ok6
    sys.exit(0 if all_passed else 1)
