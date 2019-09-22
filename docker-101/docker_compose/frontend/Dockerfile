FROM python:3.7.3
RUN pip3 install \
      flask==1.0.2 \
      redis==3.2.1
WORKDIR /app
COPY app.py .
CMD ["./app.py"]
