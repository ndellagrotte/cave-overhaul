#!/usr/bin/env python3
"""Standalone RCON client implementing Valve Source RCON protocol."""

import socket
import struct
import sys


def _pack(request_id, packet_type, payload):
    """Build an RCON packet: length(int32) + id(int32) + type(int32) + payload + \x00\x00"""
    body = struct.pack("<ii", request_id, packet_type) + payload.encode("utf-8") + b"\x00\x00"
    return struct.pack("<i", len(body)) + body


def _recv(sock):
    """Read one RCON response packet. Returns (request_id, packet_type, payload)."""
    raw_len = _recv_exact(sock, 4)
    length = struct.unpack("<i", raw_len)[0]
    data = _recv_exact(sock, length)
    request_id, packet_type = struct.unpack("<ii", data[:8])
    payload = data[8:-2].decode("utf-8", errors="replace")
    return request_id, packet_type, payload


def _recv_exact(sock, n):
    """Read exactly n bytes from socket."""
    buf = b""
    while len(buf) < n:
        chunk = sock.recv(n - len(buf))
        if not chunk:
            raise ConnectionError("Connection closed")
        buf += chunk
    return buf


def rcon(host, port, password, command):
    """Connect, authenticate, send command, return response."""
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.settimeout(30)
    sock.connect((host, port))

    # Authenticate (type 3)
    sock.sendall(_pack(1, 3, password))

    # Minecraft sends two responses to auth:
    #   1. Empty SERVERDATA_RESPONSE_VALUE (type 0, rid matches)
    #   2. SERVERDATA_AUTH_RESPONSE (type 2, rid matches or -1 on failure)
    # Read both, checking the second for auth failure.
    rid1, type1, _ = _recv(sock)
    if type1 == 2:
        # Some implementations skip the phantom — this IS the auth response
        if rid1 == -1:
            raise PermissionError("RCON authentication failed")
    else:
        # First was the phantom empty response, read actual auth response
        rid2, _, _ = _recv(sock)
        if rid2 == -1:
            raise PermissionError("RCON authentication failed")

    # Send command (type 2)
    sock.sendall(_pack(2, 2, command))
    _, _, response = _recv(sock)

    sock.close()
    return response


def main():
    if len(sys.argv) != 5:
        print(f"Usage: {sys.argv[0]} <host> <port> <password> <command>", file=sys.stderr)
        sys.exit(1)

    host = sys.argv[1]
    port = int(sys.argv[2])
    password = sys.argv[3]
    command = sys.argv[4]

    result = rcon(host, port, password, command)
    if result:
        print(result)


if __name__ == "__main__":
    main()
