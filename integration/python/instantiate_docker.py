import docker
import sys
import os
import argparse

# Please perform the following steps in order to use this script
# 1) Install pyton 3 and pip3: sudo apt install python3-pip python3
# 2) Install the docker sdk with pip: pip3 install docker

parser = argparse.ArgumentParser()
parser.add_argument("jar", help="relative or absolute path to the dda-serverspec-crate uberjar.")
parser.add_argument("config", help="relative or absolute path to the config file in edn format.")
# TODO: Review jem 2018.11.08: relevant only for debug? If yes, then remove!
parser.add_argument("-c", "--cmd", help="alternative command to execute in the docker container.\
                    Default is to run the given uberjar with the given config.")
parser.add_argument("-i", "--image", help="image for the docker container. Default image is openjdk:8 (where netstat tests do not work since net-tools is not installed).")
args = parser.parse_args()

docker_logs = os.getcwd() + '/docker-logs/'
if not os.path.exists(docker_logs):
    os.makedirs(docker_logs)

edn_file = os.path.abspath(args.config)
jar_file = os.path.abspath(args.jar)

# TODO: Review jem 2018.11.08: Put defaults to the argparse section
execute_command = 'java -jar /app/uberjar.jar /app/config.edn'
if args.cmd:
    execute_command = args.cmd

# TODO: Review jem 2018.11.08: Put defaults to the argparse section
image = 'openjdk:8'
if args.image:
    image = args.image

# TODO: Review jem 2018.11.08: we curl the serverspec outside - is'nt it a bad idea to do the curl inside of this test-script?

debug_map = {'edn_file':edn_file, 'jar_file':jar_file, 'docker_logs':docker_logs}

client = docker.APIClient()
# docker run --volume $(pwd)/example-serverspec.edn:/app/config.edn --volume $(pwd)/target/dda-serverspec-crate-1.1.4-SNAPSHOT-standalone.jar:/app/uberjar.jar --volume $(pwd)/docker_logs/:/logs/ -it openjdk:8 /bin/bash
container = client.create_container(
    image=image,
    command=execute_command,
    volumes=['/app/config.edn', '/app/uberjar.jar', '/logs'],

    host_config=client.create_host_config(binds={
        edn_file: {
            'bind': '/app/config.edn',
            'mode': 'ro',
        },
        jar_file: {
            'bind': '/app/uberjar.jar',
            'mode': 'ro',
        },
        docker_logs: {
            'bind': '/logs/',
            'mode': 'rw',
        }
    })
)

response = client.start(container=container)
for log in client.logs(container, stream=True, stdout=True, stderr=True):
    print(log)

sys.exit(client.wait(container)['StatusCode'])
