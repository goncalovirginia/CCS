![](https://media-exp1.licdn.com/dms/image/C4D1BAQFXbFOCkyU6_Q/company-background_10000/0/1612543717706?e=2147483647&v=beta&t=uMxx0Lx8R-t3Fglk10B_hbF_KvMYf87EJvoqUdtsRpQ)

# **Cloud Computing Systems** <br> Auctions Project </span><br>
### Repository: [https://github.com/goncalovirginia/CCS](https://github.com/goncalovirginia/CCS)<br>
### Gonçalo Virgínia - 56773 - g.virginia@campus.fct.unl.pt<br>
### André Correia - 64783 - aas.correia@campus.fct.unl.pt<br>
### Rodrigo Fontinha - 64813 - r.fontinha@campus.fct.unl.pt<br>

<div style="page-break-after: always"></div>

## 1. Introduction

Second project for the Cloud Computing Systems course, comprised of the Kubernetes configuration and adapted backend implementation of a scalable auction service akin to EBay.

Using the Azure Kubernetes service and Docker as the container system, the project is provisioned with an environment for application deployment and testing.

<a name="description"></a>

## 2. Project description

The solution for this assignment can be found within the [kubernetes.project](https://github.com/goncalovirginia/CCS/tree/master/kubernetes.project) directory in the project repository. This directory contains the [config](https://github.com/goncalovirginia/CCS/tree/master/kubernetes.project/config) folder where all Docker and Kubernetes related configuration files can be found as well (see [Annex section](#a1)), while the remaining folders are related to the application backend.

At first and for the sake of simplicity, some functionalities from the first project were stripped away from this project's solution. The goal was to implement the assignment-suggested replacements for such services once the application was up and running in Kubernetes, adapting the code accordingly.

In its current state, the project application can be deployed to Azure Kubernetes service, as well as its Redis caching service, and Blob Storage was replaced by a persistent volume to store media data.

There were also attempts at replacing the database system by MongoDB and Azure Functions by CronJobs, but we didn't manage to establish the necessary application connections in time for the project delivery. Traces of these unfinished and ultimately unused implementations can be found, respectively, at the *MongoDBLayer* and *MongoDBCollection* java files and the *closeAuctionsCronjob* yml file (see [Annex section](#a2)).

<div style="page-break-after: always"></div>

<a name="evaluation"></a>

## 3. Evaluation

In order to test and evaluate the application deployed to Kubernetes, the Artillery scripts used in the first project were adapted to meet testing conditions (see [Annex section](#a3)). Additionally, alike the first project's evaluation, the data provided in this section is based on response time when running the corresponding Artillery scripts.

**Note:** The target field must be changed in both scripts to match the port exposed by the Kubernetes service.

* **create-users.yml**

```
[insert results]
```

* **create-auctions.yml**

```
[insert results]
```

[insert conclusion]

<div style="page-break-after: always"></div>

## Annexes

Annex 1 - azure-app.yaml <a name="a1"></a> (Back to [Project description](#description))

```yml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: redis
spec:
  replicas: 1
  selector:
    matchLabels:
      app: redis
  template:
    metadata:
      labels:
        app: redis
    spec:
      nodeSelector:
        "kubernetes.io/os": linux
      containers:
      - name: redis
        image: redis
        env:
        - name: ALLOW_EMPTY_PASSWORD
          value: "yes"
        resources:
          requests:
            cpu: 100m
            memory: 128Mi
          limits:
            cpu: 250m
            memory: 256Mi
        ports:
        - containerPort: 6379
          name: redis
---
apiVersion: v1
kind: Service
metadata:
  name: redis
spec:
  ports:
  - port: 6379
  selector:
    app: redis
---
kind: StorageClass
apiVersion: storage.k8s.io/v1
metadata:
  name: my-azurefile
provisioner: file.csi.azure.com
allowVolumeExpansion: true
parameters:
  skuName: Standard_LRS
---
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: azure-managed-disk
spec:
  accessModes:
  - ReadWriteOnce
  storageClassName: my-azurefile
  resources:
    requests:
      storage: 1Gi
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: webapp
spec:
  replicas: 1
  selector:
    matchLabels:
      app: webapp
  template:
    metadata:
      labels:
        app: webapp
    spec:
      nodeSelector:
        "kubernetes.io/os": linux
      containers:
      - name: webapp
        image: sccproject/sccapp
        volumeMounts:
        - mountPath: "/mnt/vol"
          name: mediavolume
        resources:
          requests:
            cpu: 1000m
            memory: 1000Mi
          limits:
            cpu: 4000m
            memory: 4000Mi
        ports:
        - containerPort: 8080
        env:
        - name: REDIS_HOSTNAME
          value: "redis"
        - name: BLOB_PATH
          value: "/mnt/vol/"
        - name: MONGO_CONN
          value: "mongodb://scc-app:test@mongo:27017/"
        - name: DB_NAME
          value: "scc-mongodb"
      volumes:
      - name: mediavolume
        persistentVolumeClaim:
          claimName: azure-managed-disk
---
apiVersion: v1
kind: Service
metadata:
  name: webapp
spec:
  type: LoadBalancer
  selector:
    app: webapp
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8080
```

Annex 2 - mongo-config.yaml (Back to [Project description](#description))

```yml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: mongo-storage
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests: 
      storage: 1Gi
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: mongo
spec:
  replicas: 1
  selector:
    matchLabels:
      app: mongo
  template:
    metadata:
      labels:
        app: mongo
    spec:
      nodeSelector:
        "kubernetes.io/os": linux
      containers:
        - name: mongo
          image: mongo
          env:
            - name: "MONGO_USER"
              value: scc-app
            - name: "MONGO_PWD"
              value: test
          ports:
            - containerPort: 27017
              name: mongo
          volumeMounts:
            - mountPath: "/data/db"
              name: mongovolume
      volumes:
      - name: mongovolume
        persistentVolumeClaim:
          claimName: mongo-storage
---
apiVersion: v1
kind: Service
metadata:
  name: mongo
spec:
  type: ClusterIP
  selector:
    app: mongo
  ports:
    - protocol: TCP
      port: 27017
      targetPort: 27017
```

Annex 3 - closeAuctionCronjob.yml <a name="a2"></a> (Back to [Project description](#description))

```yml
apiVersion: v1
kind: CronJob
metadata:
  name: CloseAuctions
spec:
  schedule: "0 0 * * *"
  jobTemplate:
    spec:
      template:
        spec:
          containers:
            - name: CloseAuctions
              image: busybox:1.28
              imagePullPolicy: IfNotPresent
              command:
                - /bin/sh
                - -c
                - java CloseAuctions
          restartPolicy: OnFailure
```

Annex 4 - create-users.yml <a name="a3"></a> (Back to [Evaluation](#evaluation))

```yml
config:
  target: 'http://20.13.66.99/rest'
  http:
    timeout: 60
  plugins:
    metrics-by-endpoint:
      useOnlyRequestNames: true  # new mode to aggregate metrics in artillery
  processor: "./test-utils.js"
  variables:
    numUsers : 50
  phases:
  - name: "Create users"    # Create users
    duration: 1
    arrivalCount: 1

scenarios:
  - name: 'Create users'
    weight: 1
    flow:
      - loop:                            # let's create 50 users - loop ... count
        - post:                          # First: put image for the user
            url: "/media"
            name: "POST:/media"
            headers:
              Content-Type: application/octet-stream
              Accept: application/json
            beforeRequest: "uploadImageBody"
            capture: 
              regexp: "(.+)"
              as: "imageId"              # capture the reply as image id to be used in user creation
        - function: "genNewUser"         # Generate the needed information for the user
        - post:
            url: "/user"
            name: "POST:/user"
            headers:
              Content-Type: application/json
              Accept: application/json
            json:
              id: "{{ id }}"
              name: "{{ name }}"
              pwd: "{{ pwd }}"
              photoId: "{{ imageId }}"
            afterResponse: "genNewUserReply"    # capture result and store in file
        count: "{{ numUsers }}"
```

Annex 5 - create-auctions.yml (Back to [Evaluation](#evaluation))

```yml
config:
  target: 'http://20.13.66.99/rest'
  http:
    timeout: 300
  plugins:
    metrics-by-endpoint:
      useOnlyRequestNames: true
  processor: "./test-utils.js"
  variables:
    numAuctions : 30
    maxBids : 5 
    maxQuestions : 2
  phases:
  - name: "Create auctions"
    duration: 1
    arrivalCount: 1

scenarios:
  - name: 'Create auctions'
    weight: 1
    flow:
      - loop:                           
        - function: "selectUserSkewed"
        - post:                          
            url: "/user/auth"
            name: "POST:/user/auth"
            headers:
              Content-Type: application/json
            json:
              userId: "{{ user }}"
              pwd: "{{ pwd }}"
        - function: "genNewAuction"
        - post:                         
            url: "/media"
            name: "POST:/media"
            headers:
              Content-Type: application/octet-stream
              Accept: application/json
            beforeRequest: "uploadImageBody"
            capture: 
              regexp: "(.+)"
              as: "photoId"              
        - post:                          
            url: "/auction"
            name: "POST:/auction"
            headers:
              Content-Type: application/json
              Accept: application/json
            json:
              id: ""
              description: "{{ description }}"
              title: "{{ title }}"
              owner: "{{ user }}"
              photoId: "{{ photoId }}"
              endTime: "{{ endTime }}"
              minPrice: "{{ minimumPrice }}"
              status: "{{ status }}"
              winner: ""
              winningBid: 0
            capture:                     
              - json: $.id
                as: "auctionId"
              - json: $.owner
                as: "auctionUser"
        - loop:                         
          - function: "selectUserSkewed"
          - function: "genNewBid"
          - post:                         
              url: "/user/auth"
              name: "POST:/user/auth"
              headers:
                Content-Type: application/json
              json:
                userId: "{{ user }}"
                pwd: "{{ pwd }}"
          - post:               
              url: "/auction/{{ auctionId }}/bid"
              name: "POST:/auction/*/bid"
              headers:
                Content-Type: application/json
                Accept: application/json
              json:
                id: ""
                auction: "{{ auctionId }}"
                user: "{{ user }}"
                amount: "{{ value }}"
          count: "{{ numBids }}"   
        - loop:                    
          - function: "selectUserSkewed"
          - function: "genNewQuestion"
          - post:                         
              url: "/user/auth"
              name: "POST:/user/auth"
              headers:
                Content-Type: application/json
              json:
                userId: "{{ user }}"
                pwd: "{{ pwd }}"
          - post:                              
              url: "/auction/{{ auctionId }}/question"
              name: "POST:/auction/*/question"
              headers:
                Content-Type: application/json
                Accept: application/json
              json:
                id: ""
                auction: "{{ auctionId }}"
                user: "{{ user }}"
                text: "{{ text }}"
                answer: ""
              capture:                   
                - json: $.id
                  as: "questionId"
          - function: "genNewQuestionReply"
          - post:
              url: "/user/auth"
              name: "POST:/user/auth"
              headers:
                Content-Type: application/json
              json:
                userId: "{{ auctionUser }}"
                pwd: "{{ auctionUserPwd }}"
              ifTrue: "reply"
          - put:                          
              url: "/auction/{{ auctionId }}/question/{{ questionId }}/reply"
              name: "PUT:/auction/*/question/*/reply"
              headers:
                Content-Type: application/json
                Accept: application/json
              json:
                reply: "{{ reply }}"
              ifTrue: "reply"
          count: "{{ numQuestions }}"   
        count: "{{ numAuctions }}"
```