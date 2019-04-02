from http.server import BaseHTTPRequestHandler, HTTPServer
from datetime import datetime as dt
import random
import json


class Simple(BaseHTTPRequestHandler):

    light_names = ["main", "l1", "l2", "l3"]
    valid_states = ["ON", "OFF"]

    def _set_headers(self):
        self.send_response(200)
        self.send_header('Content-type', 'application/json')
        self.end_headers()

    def do_HEAD(self):
            self._set_headers()

    def do_POST(self):
        # Doesn't do anything with posted data
        self._set_headers()
        self.wfile.write(json.dumps({"main": "ON"}).encode())

    def do_GET(self):
        self._set_headers()
        if self.path == "/timer":
            self._get_fake_timers()
        elif self.path == "/lights":
            self._get_fake_light_status()
        else:
            print("path '{}' is not a valid path".format(self.path))

    def _get_fake_timers(self):
        # generate some random timer data
        entries = []
        for _ in range(random.randint(1, 10)):
            now = dt.now().timestamp()
            entries.append((
                random.choice(self.light_names),
                random.choice(self.valid_states),
                dt.fromtimestamp(now + random.randint(0, 3600)).isoformat()  # sometime between now and 1h
            ))
        self.wfile.write(json.dumps(entries).encode())

    def _get_fake_light_status(self):
        # generate random light states
        statuses = dict([
            (k, random.choice(self.valid_states))
            for k in self.light_names
        ])
        print(statuses)
        self.wfile.write(json.dumps(statuses).encode())




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
