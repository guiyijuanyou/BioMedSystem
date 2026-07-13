#!/usr/bin/env python3
"""Call the school SOAP endpoint using the project's HMAC signing protocol."""

from __future__ import annotations

import hashlib
import hmac
import os
from pathlib import Path
import secrets
import sys
import time
import urllib.error
import urllib.request


BASE_URL = os.getenv("BIOMED_BASE_URL", "http://localhost:8088").rstrip("/")
CLIENT_CODE = os.getenv("BIOMED_INTEGRATION_CLIENT", "")
CLIENT_SECRET = os.getenv("BIOMED_INTEGRATION_SECRET", "")
SOAP_PATH = "/api/soap/school"
SOAP_ACTION = "http://cqutcm.com/biomed/school/v1/queryGrowthData"


def canonical_request(body: bytes, timestamp: str, nonce: str) -> str:
    body_digest = hashlib.sha256(body).hexdigest()
    return "\n".join(("POST", SOAP_PATH, timestamp, nonce, body_digest))


def main() -> int:
    if not CLIENT_CODE or not CLIENT_SECRET:
        print(
            "Set BIOMED_INTEGRATION_CLIENT and BIOMED_INTEGRATION_SECRET first.",
            file=sys.stderr,
        )
        return 2

    body = Path(__file__).with_name("soap-request.xml").read_bytes()
    timestamp = str(int(time.time()))
    nonce = secrets.token_urlsafe(24)
    canonical = canonical_request(body, timestamp, nonce)
    signature = hmac.new(
        CLIENT_SECRET.encode("utf-8"), canonical.encode("utf-8"), hashlib.sha256
    ).hexdigest()

    request = urllib.request.Request(
        BASE_URL + SOAP_PATH,
        data=body,
        method="POST",
        headers={
            "Content-Type": "text/xml; charset=utf-8",
            "SOAPAction": f'"{SOAP_ACTION}"',
            "X-Integration-Client": CLIENT_CODE,
            "X-Timestamp": timestamp,
            "X-Nonce": nonce,
            "X-Signature": signature,
            "X-Request-Id": "soap-" + secrets.token_hex(12),
        },
    )

    try:
        with urllib.request.urlopen(request, timeout=30) as response:
            print("HTTP", response.status)
            print("X-Request-Id:", response.headers.get("X-Request-Id"))
            print(response.read().decode("utf-8"))
            return 0
    except urllib.error.HTTPError as error:
        print("HTTP", error.code, file=sys.stderr)
        print("X-Request-Id:", error.headers.get("X-Request-Id"), file=sys.stderr)
        print(error.read().decode("utf-8", errors="replace"), file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
