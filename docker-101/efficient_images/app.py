#!/usr/bin/env python3

import click

@click.command()
def hello():
    click.echo('Hello SuperOrbital!')

if __name__ == '__main__':
    hello()
