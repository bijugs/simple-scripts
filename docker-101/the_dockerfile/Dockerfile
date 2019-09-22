# Welcome to your (completely empty) Dockerfile!
#
# If you're lost, you can view
# https://docs.docker.com/engine/reference/builder/ for documentation on each
# avilable instruction.
FROM python:3.7.3
WORKDIR /app
COPY requirements.txt .
RUN pip3 install -r requirements.txt
COPY app.py .
CMD ["python3","app.py"]
