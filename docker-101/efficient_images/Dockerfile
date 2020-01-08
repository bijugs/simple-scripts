FROM python:3.7.3

RUN apt-get update && \
    apt-get install -yq \
      emacs \
      nano \
      vim 

RUN pip install click==7.0
RUN pip install flask

WORKDIR /app

COPY . .

CMD ["/app/app.py"]
