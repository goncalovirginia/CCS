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

<a name="evaluation"></a>

## 3. Evaluation

In order to test and evaluate the application deployed to Kubernetes, the Artillery scripts used in the first project were adapted to meet testing conditions (see [Annex section](#a3)). Additionally, alike the first project's evaluation, the data provided in this section is based on response time when running the corresponding Artillery scripts.

**Note:** The target field must be changed in both scripts to match the application IP exposed by the Kubernetes service.

* **create-users.yml**

```
http.response_time:
  min: ............................................................ 53
  max: ............................................................ 2458
  median: ......................................................... 96.6
  p95: ............................................................ 278.7
  p99: ............................................................ 713.5
```

<div style="page-break-after: always"></div>

* **create-auctions.yml**

```
http.response_time:
  min: ............................................................ 54
  max: ............................................................ 382
  median: ......................................................... 80.6
  p95: ............................................................ 198.4
  p99: ............................................................ 198.4
```

* **workload1.yml**

```
http.response_time:
  min: ............................................................ 47
  max: ............................................................ 2961
  median: ......................................................... 82.3
  p95: ............................................................ 214.9
  p99: ............................................................ 944
```

Looking back on the first project's conceived deployment settings, we had:

| Alias | Setting |
| --- | --- |
| **First setting** | Deployment on Azure in the West Europe region, with caching |
| **Best solution** | Deployment on Azure in the West Europe region, without caching |

The minimum values for response time remain close to identical to the first project's deployment settings. The maximum response time and median values seem to have improved compared to the first setting, with the 95 and 99 latency percentile values also seeing some ups and downs.

Overall, the results indicate that **performance has improved** when compared to the first project's first deployment setting. Although this improvement wasn't significant enough to take place as the best solution for our application's environment, there is an argument to be made regarding deployment on Kubernetes. Given that the caching service somewhat hindered performance in the first project, Kubernetes seems to be a very good alternative, as the application's performance improved to the point of being relatively competitive towards our best solution with little to no changes to the Redis caching service's implementation.

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

Annex 6 - workload1.yml (Back to [Evaluation](#evaluation))

```yml
config:
  target: 'http://20.31.193.170/rest'
  http:
    timeout: 10
  plugins:
    metrics-by-endpoint:
      useOnlyRequestNames: true
  processor: "./test-utils.js"
  phases:
  - name: "Warm up"
    arrivalRate: 1         
    duration: 10
  - name: "Test"
    arrivalRate: 5         
    duration: 60

scenarios:
  - name: 'User checks own auctions'
    weight: 5
    flow:
      - function: "selectUserSkewed"
      - post:                       
          url: "/user/auth"
          name: "POST:/user/auth"
          headers:
            Content-Type: application/json
          json:
            userId: "{{ user }}"
            pwd: "{{ pwd }}"
      - get:                       
          url: "/auction/owner/{{ user }}"
          name: "GET:/user/*/auctions"
          headers:
            Accept: application/json
          capture: 
            json: "$"
            as: "auctionsLst"
      - loop:                                  
        - get:                         
            url: "/media/{{ $loopElement.photoId }}"
            name: "GET:/media"
            headers:
              Accept: application/octet-stream
        - get:
            url: "/auction/{{ $loopElement.id }}/bid"
            name: "GET:/auction/*/bid"
            headers: 
              Accept: application/json
        - get:
            url: "/auction/{{ $loopElement.id }}/question"
            name: "GET:/auction/*/question"
            headers: 
              Accept: application/json
            capture: 
              json: "$"
              as: "questionOne"
        - function: "decideToReply"
        - post:                              
            url: "/auction/{{ $loopElement.id }}/question/{{ questionOne.id }}/reply"
            name: "ªPOST:/auction/*/question/*/reply"
            headers:
              Content-Type: application/json
              Accept: application/json
            json:
              answer: "{{ reply }}"
            ifTrue: "reply"
        over: "auctionsLst"

  - name: 'Mixed browsing'
    weight: 40
    flow:
      - function: "selectUserSkewed"
      - post:                   
          url: "/user/auth"
          name: "POST:/user/auth"
          headers:
            Content-Type: application/json
          json:
            userId: "{{ user }}"
            pwd: "{{ pwd }}"
      - loop:                                  
        - function: "decideNextAction"
        - get:                        
            url: "/auction/owner/{{ user }}"
            name: "GET:/user/*/auctions"
            headers:
              Accept: application/json
            capture: 
              json: "$"
              as: "auctionsLst"
            ifTrue: "nextAction == 2"
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
            ifTrue: "nextAction == 3"
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
            ifTrue: "nextAction == 3"
        - get:                          
            url: "/media/{{ photoId }}"
            name: "GET:/media"
            headers:
              Accept: application/octet-stream
            ifTrue: "nextAction >= 4"
        - get:
            url: "/auction/{{ auctionId }}/bid" 
            name: "GET:/auction/*/bid"
            headers: 
              Accept: application/json
            capture: 
              json: "$"
              as: "bidsLst"
            ifTrue: "nextAction >= 4"
        - get:
            url: "/auction/{{ auctionId }}/question"
            name: "GET:/auction/*/question"
            headers: 
              Accept: application/json
            capture: 
              json: "$"
              as: "bidsLst"
            ifTrue: "nextAction >= 4"
        - function: "decideToCoverBid"
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
              amount: "{{ bidValue }}"
            ifTrue: "nextAction == 5"
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
            ifTrue: "nextAction == 6"
        whileTrue: "random80"
```

Annex 7 - test-utils.js (Back to [Evaluation](#evaluation))

```js
'use strict';

/***
 * Exported functions to be used in the testing scripts.
 */
module.exports = {
  uploadImageBody,
  genNewUser,
  genNewUserReply,
  selectUser,
  selectUserSkewed,
  genNewAuction,
  genNewBid,
  genNewQuestion,
  genNewQuestionReply,
  decideToCoverBid,
  decideToReply,
  decideNextAction,
  random80,
  random50,
}

const Faker = require('faker')
const fs = require('fs')
const path = require('path')

var images = []
var users = []
let userPassword = new Map();

// Auxiliary function to select an element from an array
Array.prototype.sample = function(){
	   return this[Math.floor(Math.random()*this.length)]
}

// Auxiliary function to select an element from an array
Array.prototype.sampleSkewed = function(){
	return this[randomSkewed(this.length)]
}

// Returns a random value, from 0 to val
function random( val){
	return Math.floor(Math.random() * val)
}

// Returns the user with the given id
function findUser( id){
	for( var u of users) {
		if( u.id === id)
			return u;
	}
	return null
}

// Returns a random value, from 0 to val
function randomSkewed( val){
	let beta = Math.pow(Math.sin(Math.random()*Math.PI/2),2)
	let beta_left = (beta < 0.5) ? 2*beta : 2*(1-beta);
	return Math.floor(beta_left * val)
}

// Loads data about images from disk
function loadData() {
	var basedir
	if( fs.existsSync( '/images')) 
		basedir = '/images'
	else
		basedir =  'images'	
	fs.readdirSync(basedir).forEach( file => {
		if( path.extname(file) === ".jpeg") {
			var img  = fs.readFileSync(basedir + "/" + file)
			images.push( img)
		}
	})
	var str;
	if( fs.existsSync('users.data')) {
		str = fs.readFileSync('users.data','utf8')
		users = JSON.parse(str)
	} 
}

loadData();

/**
 * Sets the body to an image, when using images.
 */
function uploadImageBody(requestParams, context, ee, next) {
	requestParams.body = images.sample()
	return next()
}

/**
 * Generate data for a new user using Faker
 */
function genNewUser(context, events, done) {
	const first = `${Faker.name.firstName()}`
	const last = `${Faker.name.lastName()}`
	context.vars.id = first + "." + last
	context.vars.name = first + " " + last
	context.vars.pwd = `${Faker.internet.password()}`
	userPassword.set(context.vars.id, context.vars.pwd);
	return done()
}

/**
 * Process reply for of new users to store the id on file
 */
function genNewUserReply(requestParams, response, context, ee, next) {
	if( response.statusCode >= 200 && response.statusCode < 300 && response.body.length > 0)  {
		let u = JSON.parse( response.body)
		u.pwd = userPassword.get(u.id)
		users.push(u)
		fs.writeFileSync('users.data', JSON.stringify(users));
	}
    return next()
}

/**
 * Select user
 */
function selectUser(context, events, done) {
	if( users.length > 0) {
		let user = users.sample()
		context.vars.user = user.id
		context.vars.pwd = user.pwd
	} else {
		delete context.vars.user
		delete context.vars.pwd
	}
	return done()
}

/**
 * Select user
 */
function selectUserSkewed(context, events, done) {
	if( users.length > 0) {
		let user = users.sampleSkewed()
		context.vars.user = user.id
		context.vars.pwd = user.pwd
	} else {
		delete context.vars.user
		delete context.vars.pwd
	}
	return done()
}

/**
 * Generate data for a new auction
 * Besides the variables for the auction, initializes the following vars:
 * numBids - number of bids to create, if batch creating 
 * numQuestions - number of questions to create, if batch creating 
 * bidValue - price for the next bid
 */
function genNewAuction(context, events, done) {
	context.vars.title = `${Faker.commerce.productName()}`
	context.vars.description = `${Faker.commerce.productDescription()}`
	context.vars.minimumPrice = `${Faker.commerce.price(10, 1000, 0)}`
	context.vars.bidValue = context.vars.minimumPrice + random(3)
	var maxBids = 5
	if( typeof context.vars.maxBids !== 'undefined')
		maxBids = context.vars.maxBids;
	var maxQuestions = 2
	if( typeof context.vars.maxQuestions !== 'undefined')
		maxQuestions = context.vars.maxQuestions;
	var d = new Date();
	d.setTime(Date.now() + random( 300000));
	context.vars.endTime = d.toISOString();
	if( Math.random() > 0.2) { 
		context.vars.status = "OPEN";
		context.vars.numBids = random( maxBids);
		context.vars.numQuestions = random( maxQuestions);
	} else {
		context.vars.status = "CLOSED";
		delete context.vars.numBids;
		delete context.vars.numQuestions;
	}
	return done()
}

/**
 * Generate data for a new bid
 */
function genNewBid(context, events, done) {
	if( typeof context.vars.bidValue == 'undefined') {
		if( typeof context.vars.minimumPrice == 'undefined') {
			context.vars.bidValue = random(100)
		} else {
			context.vars.bidValue = +context.vars.minimumPrice + random(3)
		}
	}
	context.vars.value = +context.vars.bidValue;
	context.vars.bidValue = +context.vars.bidValue + 1 + random(3)
	return done()
}

/**
 * Generate data for a new question
 */
function genNewQuestion(context, events, done) {
	context.vars.text = `${Faker.lorem.paragraph()}`;
	return done()
}

/**
 * Generate data for a new reply
 */
function genNewQuestionReply(context, events, done) {
	delete context.vars.reply;
	if( Math.random() > 0.5) {
		if( typeof context.vars.auctionUser !== 'undefined') {
			var user = findUser( context.vars.auctionUser);
			if( user != null) {
				context.vars.auctionUserPwd = user.pwd;
				context.vars.reply = `${Faker.lorem.paragraph()}`;
			}
		}
	} 
	return done()
}

/**
 * Decide whether to bid on auction or not
 * assuming: user context.vars.user; bids context.vars.bidsLst
 */
function decideToCoverBid(context, events, done) {
	delete context.vars.value;
	if( typeof context.vars.user !== 'undefined' && typeof context.vars.bidsLst !== 'undefined' && 
			context.vars.bidsLst.constructor == Array && context.vars.bidsLst.length > 0) {
		let bid = context.vars.bidsLst[0];
		if( bid.user !== context.vars.user && Math.random() > 0.5) {
			context.vars.value = bid.value + random(3);
			context.vars.auctionId = bid.auctionId;
		}
	}
	return done()
}

/**
 * Decide whether to reply
 * assuming: user context.vars.user; question context.vars.questionOne
 */
function decideToReply(context, events, done) {
	delete context.vars.reply;
	if( typeof context.vars.user !== 'undefined' && typeof context.vars.questionOne !== 'undefined' && 
			context.vars.questionOne.user === context.vars.user && 
			typeof context.vars.questionOne.reply !== String &&
			Math.random() > 0) {
		context.vars.reply = `${Faker.lorem.paragraph()}`;
	}
	return done()
}

function decideNextAction(context, events, done) {
	delete context.vars.auctionId;
	let rnd = Math.random()
	if( rnd < 0.225)
		context.vars.nextAction = 2; // browsing user auctions
	else if( rnd < 0.4)
		context.vars.nextAction = 3; // create an auction
	else if( rnd < 0.8)
		context.vars.nextAction = 4; // checking auction
	else if( rnd < 0.95)
		context.vars.nextAction = 5; // do a bid
	else
		context.vars.nextAction = 6; // post a message

	if( context.vars.nextAction == 2) {
		if( Math.random() < 0.5)
			if(typeof context.vars.user2 != 'undefined')
				context.vars.user = context.vars.user2
		else {
			let user = users.sample()
			context.vars.user2 = user.id
		}
	}

	if( context.vars.nextAction == 3) {
		context.vars.title = `${Faker.commerce.productName()}`
		context.vars.description = `${Faker.commerce.productDescription()}`
		context.vars.minimumPrice = `${Faker.commerce.price(10, 1000, 0)}`
		context.vars.bidValue = context.vars.minimumPrice + random(3)
		var d = new Date();
		d.setTime(Date.now() + 60000 + random( 300000));
		context.vars.endTime = d.toISOString();
		context.vars.status = "OPEN";
	}
	if(context.vars.nextAction >= 4) {
		if(typeof context.vars.auctionsLst == 'undefined')
			return decideNextAction(context,events,done);

		let auct = context.vars.auctionsLst.sample();
		if(typeof auct == 'undefined')
			return decideNextAction(context,events,done);
		context.vars.auctionId = auct.id
		context.vars.photoId = auct.photoId
	}
	if( context.vars.nextAction == 6)
		context.vars.text = `${Faker.lorem.paragraph()}`;

	return done()
}

/**
 * Return true with probability 50% 
 */
function random50(context, next) {
  const continueLooping = Math.random() < 0.5
  return next(continueLooping);
}

/**
 * Return true with probability 80% 
 */
function random80(context, next) {
  const continueLooping = Math.random() < 0.8
  return next(continueLooping);
}
```