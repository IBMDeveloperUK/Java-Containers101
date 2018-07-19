# Lab 0: Prerequisites

To run the labs, there are several requirements:
1. [Install Docker](#1-install-docker)
2. [Install kubectl](#2-install-kubectl)
3. [Install Minikube](#3-install-minikube)
4. [Configure your Minikube cluster with kubectl](#4-configuring-a-minikube-cluster-with-kubectl)

## 1. Install Docker

Before you start running any of the exercises, make sure you have downloaded the appropriate version of [Docker](https://docs.docker.com/get-started/) for your OS. Docker is the underlying container engine that we will use to deploy our Java containers providing a comprehensive ecosystem around manipulating containers (run, commit, start, stop, delete, pull, push). We could have used Docker alone but with some of the labs we want to cover in the workshop, it would quickly become unclear and untidy.  

### Docker CE for Linux users

Select your appropriate Linux distribution to install [Docker CE](https://docs.docker.com/install/#server) from the Docker store. 

*Note:* you will be prompted to create an account on the Docker store if you don't already have one.

### Docker Toolbox for Windows and Mac users

It is possible to run Docker CE on Mac and on Microsoft Windows 10 Professional or Enterprise 64-bit, but as another requirement for Mac and Windows is to have VirtualBox installed (as you will see later on), we will just use Docker Toolbox since this will install VirtualBox for us. 

For Windows users install [Docker Toolbox for Windows](https://docs.docker.com/toolbox/toolbox_install_windows/).

For Mac users install [Docker Toolbox for Mac](https://docs.docker.com/toolbox/toolbox_install_mac/).

Once the install is complete, open VirtualBox and update to the latest version (This may require you to download another executable or image file, but is necessary to resolve issues related to running Minikube as detailed below).

### Verifying Install

Open your chosen terminal or the Docker Quick Start Terminal and run the following command to verify a successful install:

```shell
docker version
```

If you get an error like `Cannot connect to the Docker daemon at tcp://<ip>:<port>. Is the docker daemon running?` on Linux when trying to run this command, you will probably need to prepend the command with `sudo`. In order to avoid using sudo everytime you run docker, create add add the `docker` group:

Create the `docker` user group:

```shell
sudo groupadd docker
```

Add your user to the `docker` group:

```shell
sudo usermod -aG docker $USER
```

## 2. Install kubectl

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

### Verifying Installation 
Verify your install by ensuring you can get the kubectl version:

```shell
kubectl version
``` 

## 3. Install Minikube

Once you have successfully installed kubectl, you will now need to install [Minikube](https://github.com/kubernetes/minikube). This will deploy a single node instance of Kubernetes on our machines and will enable us to have more granular control over how to run our containers. 

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

## 4. Configuring a Minikube cluster with kubectl

We now have Docker on our local machine either in the form of Docker Toolbox or Docker CE along with kubectl and minikube. Now we want to set up a minikube cluster. Minikube will configure all the services to have a working Kubernetes environment for a single node and kubectl will allow us to interact with the cluster from our machine. The difference between Docker Toolbox and Docker CE is that the cluster and all the containers will be provisioned within a VM that VirtualBox will manage, whereas Docker CE will deploy everything directly on the machine. The kubectl tool will allow us to interact directly with the cluster in both cases directly from our machine so post-installation, the experience will be exactly the same.

To configure a minikube cluster with Docker Toolbox's Virtual Box run:

```shell
sudo minikube start --vm-driver=virtualbox
```

To configure a minikube cluster with Docker CE run:

```shell
sudo minikube start
```

This may take 5 mins or so, so go make a cup of tea!

We can then tell `kubectl` to connect to our cluster with the following command:

```shell
sudo kubectl config use-context minikube
```

If we had another Kubernetes cluster running, this will allow us to switch between them without polluting running pods between clusters.  

As we are using kubeadm provisioner (the default provisioner) locally, we have to use 'sudo' when we are communicating with **both minikube and kubectl** (See the [git issue](https://github.com/kubernetes/kubeadm/issues/57)).

## Verify you can access your cluster

At this point we will have no running *pods* (which can be thought of in most cases as a container) in Minikube with the exception of the pods Minikube requires for it to run. Let's use these pods to see if our cluster is working and ready to use:

```shell
sudo kubectl get po --namespace=kube-system
```

You should see the following output where all the pods are in the running state:

```shell
NAME                                    READY     STATUS    RESTARTS   AGE
etcd-minikube                           1/1       Running   0          33m
kube-addon-manager-minikube             1/1       Running   0          33m
kube-apiserver-minikube                 1/1       Running   0          33m
kube-controller-manager-minikube        1/1       Running   0          34m
kube-dns-86f4d74b45-8tqqz               3/3       Running   0          34m
kube-proxy-l2qmh                        1/1       Running   0          34m
kube-scheduler-minikube                 1/1       Running   0          34m
kubernetes-dashboard-5498ccf677-9c4hr   1/1       Running   0          34m
storage-provisioner                     1/1       Running   0          34m
```

And bam! Well done for being persistent, you're now ready to run the workshop.

Continue on to [Lab 1](../Lab_1.md)
