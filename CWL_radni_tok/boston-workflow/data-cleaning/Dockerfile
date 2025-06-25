FROM python:3.10-slim

RUN apt-get update && apt-get install -y python3-pip && rm -rf /var/lib/apt/lists/*
RUN pip3 install pandas

COPY clean_data.py /app/clean_data.py
