FROM python:3.7.3

WORKDIR /app

COPY . .

RUN pip install click==7.0

RUN apt-get update && \
    apt-get install -yq \
      emacs \
      nano \
      vim 

CMD ["/app/app.py"]
