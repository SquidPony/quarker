#!/usr/bin/env python3

from __future__ import annotations

import argparse
import contextlib
import http.server
import os
import re
from http import HTTPStatus
from pathlib import Path


RANGE_RE = re.compile(r"bytes=(\d*)-(\d*)\Z")


class RangeRequestHandler(http.server.SimpleHTTPRequestHandler):
    server_version = "QuarkerRangeHTTP/1.0"

    def __init__(self, *args, directory: str | None = None, **kwargs):
        super().__init__(*args, directory=directory, **kwargs)

    def list_directory(self, path: str):
        self.send_error(HTTPStatus.FORBIDDEN, "Directory listing is disabled")
        return None

    def end_headers(self):
        self.send_header("Cache-Control", "no-store")
        super().end_headers()

    def send_head(self):
        path = self.translate_path(self.path)
        if os.path.isdir(path):
            for index in ("index.html", "index.htm"):
                candidate = os.path.join(path, index)
                if os.path.exists(candidate):
                    path = candidate
                    break
            else:
                self.send_error(HTTPStatus.FORBIDDEN, "Directory listing is disabled")
                return None

        ctype = self.guess_type(path)

        try:
            file_obj = open(path, "rb")
        except OSError:
            self.send_error(HTTPStatus.NOT_FOUND, "File not found")
            return None

        try:
            file_stat = os.fstat(file_obj.fileno())
            file_size = file_stat.st_size
            range_header = self.headers.get("Range")
            byte_range = self._parse_range_header(range_header, file_size)

            if byte_range is None:
                self.send_response(HTTPStatus.OK)
                self.send_header("Content-type", ctype)
                self.send_header("Content-Length", str(file_size))
                self.send_header("Accept-Ranges", "bytes")
                self.send_header("Last-Modified", self.date_time_string(file_stat.st_mtime))
                self.end_headers()
                self._range = None
                return file_obj

            start, end = byte_range
            content_length = end - start + 1
            self.send_response(HTTPStatus.PARTIAL_CONTENT)
            self.send_header("Content-type", ctype)
            self.send_header("Content-Length", str(content_length))
            self.send_header("Content-Range", f"bytes {start}-{end}/{file_size}")
            self.send_header("Accept-Ranges", "bytes")
            self.send_header("Last-Modified", self.date_time_string(file_stat.st_mtime))
            self.end_headers()
            file_obj.seek(start)
            self._range = (start, end)
            return file_obj
        except Exception:
            file_obj.close()
            raise

    def copyfile(self, source, outputfile):
        byte_range = getattr(self, "_range", None)
        if byte_range is None:
            super().copyfile(source, outputfile)
            return

        start, end = byte_range
        remaining = end - start + 1
        while remaining > 0:
            chunk = source.read(min(64 * 1024, remaining))
            if not chunk:
                break
            outputfile.write(chunk)
            remaining -= len(chunk)

    def _parse_range_header(self, header: str | None, file_size: int):
        if not header:
            return None

        match = RANGE_RE.fullmatch(header.strip())
        if not match:
            self.send_error(HTTPStatus.REQUESTED_RANGE_NOT_SATISFIABLE, "Invalid Range header")
            return None

        start_str, end_str = match.groups()
        if not start_str and not end_str:
            self.send_error(HTTPStatus.REQUESTED_RANGE_NOT_SATISFIABLE, "Invalid Range header")
            return None

        if start_str:
            start = int(start_str)
            end = int(end_str) if end_str else file_size - 1
        else:
            suffix_length = int(end_str)
            if suffix_length <= 0:
                self.send_error(HTTPStatus.REQUESTED_RANGE_NOT_SATISFIABLE, "Invalid Range header")
                return None
            start = max(file_size - suffix_length, 0)
            end = file_size - 1

        if start >= file_size or start > end:
            self.send_error(HTTPStatus.REQUESTED_RANGE_NOT_SATISFIABLE, "Range out of bounds")
            return None

        end = min(end, file_size - 1)
        return start, end


def main() -> None:
    parser = argparse.ArgumentParser(description="Serve Quarker web bundles with HTTP Range support.")
    parser.add_argument("--directory", default=".", help="Directory to serve")
    parser.add_argument("--port", type=int, default=8000, help="Port to bind")
    args = parser.parse_args()

    web_root = Path(args.directory).resolve()
    if not web_root.is_dir():
        raise SystemExit(f"Directory does not exist: {web_root}")

    handler = lambda *handler_args, **handler_kwargs: RangeRequestHandler(
        *handler_args,
        directory=str(web_root),
        **handler_kwargs,
    )

    with contextlib.closing(http.server.ThreadingHTTPServer(("127.0.0.1", args.port), handler)) as httpd:
        print(f"Serving {web_root} at http://127.0.0.1:{args.port}/")
        try:
            httpd.serve_forever()
        except KeyboardInterrupt:
            pass


if __name__ == "__main__":
    main()