#!/bin/sh
while true; do
    inotifywait --monitor /home/david/Desktop/sda1/Senior/misc_pog_stuff/POG-main/POG-main/back_end/rest-service/tmp -e create -e moved_to |
        python3 /home/david/Desktop/sda1/Senior/misc_pog_stuff/POG-main/POG-main/back_end/rest-service/tmp/backend_scholar_scraper.py
done
