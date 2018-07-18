# Lab 0: Prerequisites

To run the labs, there are several requirements:
1. [Docker]()
2. [kubectl]()
3. [Minikube]()

## Install Docker

Before you start running any of the exercises, make sure you have downloaded Docker CE from the [Docker store](https://store.docker.com/search?type=edition&offering=community). Docker is the container engine that we will use to deploy our Java containers providing a comprehensive ecosystem around manipulating containers (run, commit, start, stop, delete, pull, push). 

*Note:* you will be prompted to create an account on the Docker store if you don't already have one.

### Docker CE for Windowas

If you are using Windows, once you have installed Docker, you will need to ensure that you [switch to Linux Containers](https://docs.docker.com/docker-for-windows/#switch-between-windows-and-linux-containers).

### Docker Toolbox for Windows 7/8 users

Docker CE is only compatible with Microsoft Windows 10 Professional or Enterprise 64-bit. For people running other versions of Windows, you will need to install [Docker Toolbox](https://docs.docker.com/toolbox/toolbox_install_windows/).

## Install kubectl

As we will be using Minikube to deploy our containers, we will need the [kubectl](https://kubernetes.io/docs/tasks/tools/install-kubectl/#install-kubectl). It's purpose is to interact with the Kubernetes API which will be responsible for creating our kubernetes PODs (which in most cases translates to a Docker container) within our Minikube cluster. This will allow us to define our config for our containers in YAML rather than through multi-line Docker run commands which becomes quite cumbersome.

### macOS

Install with [Homebrew](https://brew.sh/):
```shell
brew install kubernetes-cli
```

### Linux (CentOS, RHEL, Fedora)

Install with yum:
```shell
cat <<EOF > /etc/yum.repos.d/kubernetes.repo
[kubernetes]
name=Kubernetes
baseurl=https://packages.cloud.google.com/yum/repos/kubernetes-el7-x86_64
enabled=1
gpgcheck=1
repo_gpgcheck=1
gpgkey=https://packages.cloud.google.com/yum/doc/yum-key.gpg https://packages.cloud.google.com/yum/doc/rpm-package-key.gpg
EOF
yum install -y kubectl
```

### Linux (Ubuntu, Debian, HypriotOS)

Install with apt-get:
```shell
sudo apt-get update && sudo apt-get install -y apt-transport-https
curl -s https://packages.cloud.google.com/apt/doc/apt-key.gpg | sudo apt-key add -
sudo touch /etc/apt/sources.list.d/kubernetes.list 
echo "deb http://apt.kubernetes.io/ kubernetes-xenial main" | sudo tee -a /etc/apt/sources.list.d/kubernetes.list
sudo apt-get update
sudo apt-get install -y kubectl
```

### Windows

Install with [Chocolatey](https://chocolatey.org/):
```shell
choco install kubernetes-cli
```

## Verifying Installation 
Verify your install by ensuring you can get the kubectl version:

```shell
kubectl version
``` 

## Install Minikube

Once you have successfully installed Docker, you will now need to install [Minikube](https://github.com/kubernetes/minikube). This will deploy a single node instance of Kubernetes on our machines and will enable us to have more granular control over how to run our containers. 

### macOS

Install with [Homebrew](https://brew.sh/):
```shell
brew cask install minikube
```

### Linux

Install with curl:
```shell
curl -Lo minikube https://storage.googleapis.com/minikube/releases/latest/minikube-linux-amd64 && chmod +x minikube && sudo mv minikube /usr/local/bin/
```

### Windows

Install with [Chocolatey](https://chocolatey.org/):
```shell
choco install minikube
```
**OR**

Install manually: Download the [minikube-windows-amd64.exe](https://storage.googleapis.com/minikube/releases/latest/minikube-windows-amd64.exe) file, rename it to `minikube.exe` and add it to your path.

