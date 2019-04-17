from http.server import BaseHTTPRequestHandler, HTTPServer
from urllib.parse import urlparse, parse_qs, parse_qsl
from datetime import datetime as dt
import random
import json


LIGHT_NAMES = ["main", "l1", "l2", "l3"]
VALID_STATES = [True, False]

TIMERS = []
LIGHT_STATUS = dict([(name, True) for name in LIGHT_NAMES])



class Simple(BaseHTTPRequestHandler):


    def _get_url_params(self):
        d = {}
        if "?" in self.path:
            for key, value in dict(parse_qsl(self.path.split("?")[1], True)).items():
                d[key] = value
        return d

    def _get_payload(self):
        d = {}
        if self.rfile:
            payload_str = self.rfile.read(int(self.headers['Content-Length']))
            payload = json.loads(payload_str)
            for key, value in payload.items():
                d[key] = value
        return d

    def _set_headers(self):
        self.send_response(200)
        self.send_header('Content-type', 'application/json')
        self.end_headers()

    def do_HEAD(self):
            self._set_headers()

    def do_POST(self):
        # Doesn't do anything with posted data
        self._set_headers()
        payload = self._get_payload()
        if self.path == "/timer":
            self._add_timer(payload)
        elif self.path == "/lights":
            self._switch_light_status(payload)
        else:
            print("path '{}' is not a valid path".format(self.path))

    def do_GET(self):
        self._set_headers()
        if self.path == "/timer":
            self._get_timers()
        elif self.path == "/lights":
            self._get_light_status()
        else:
            print("path '{}' is not a valid path".format(self.path))

    def _add_timer(self, payload):
        # update the timers with the new info
        # payload expected:
        # { light_name: {signal: bool, delay: int} }
        for k, v in payload.items():
            now = dt.now().timestamp()
            switch_time = dt.fromtimestamp(now + v['delay']).isoformat()  # time = now + delay (in seconds)
            TIMERS.append((k, v['signal'], switch_time))
        print("Adding timer. Current timers: {}".format(TIMERS))
        self.wfile.write(json.dumps(TIMERS).encode())

    def _get_timers(self):
        print("Serving timers: {}".format(TIMERS))
        self.wfile.write(json.dumps(TIMERS).encode())

    def _switch_light_status(self, payload):
        print("Light switch payload: {}".format(payload))
        LIGHT_STATUS.update(payload)
        print("Updating light status: {}".format(LIGHT_STATUS))
        self.wfile.write(json.dumps(payload).encode())

    def _get_light_status(self):
        print("Serving light status: {}".format(LIGHT_STATUS))
        self.wfile.write(json.dumps(LIGHT_STATUS).encode())




def run(server_class=HTTPServer, handler_class=Simple, port=5000):
    server_address = ('', port)
    httpd = server_class(server_address, handler_class)
    print('Starting httpd...')
    httpd.serve_forever()

if __name__ == "__main__":
    from sys import argv

    if len(argv) == 2:
        run(port=int(argv[1]))
    else:
        run()
