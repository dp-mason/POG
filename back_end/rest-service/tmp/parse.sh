#!/bin/sh
while true; do
    inotifywait --monitor /home/davidpattenmason/POG-DB/back_end/rest-service/tmp -e create -e moved_to |
        python3 /home/davidpattenmason/POG-DB/back_end/rest-service/tmp/backend_scholar_scraper.py
done
