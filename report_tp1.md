![](https://media-exp1.licdn.com/dms/image/C4D1BAQFXbFOCkyU6_Q/company-background_10000/0/1612543717706?e=2147483647&v=beta&t=uMxx0Lx8R-t3Fglk10B_hbF_KvMYf87EJvoqUdtsRpQ)

# **Cloud Computing Systems** <br> Auctions Project </span><br>
### Repository: [https://github.com/goncalovirginia/CCS](https://github.com/goncalovirginia/CCS)<br>
### Gonçalo Virgínia - 56773 - g.virginia@campus.fct.unl.pt<br>
### André Correia - 64783 - aas.correia@campus.fct.unl.pt<br>
### Rodrigo Fontinha - 64813 - r.fontinha@campus.fct.unl.pt<br>

<div style="page-break-after: always"></div>

## 1. Introduction

Main project for the Cloud Computing Systems course, comprised of the backend implementation of a scalable auction system akin to EBay.

Using Azure as the cloud provider, the project is provisioned with both a platform for application deployment in the Azure cloud and features that support the project implementation process and allow for more efficient programming.

## 2. Structure

This project is separated into two components, the **auction.project**, consisting of the main application with which users can interact with, and **fun.project**, an auxiliary service employing Azure Functions, that has extremely useful trigger integrations with other Azure services, to easily manage or build upon them.

Among the various globally available services that Azure provides, this project aims to take advantage of the following:

* **Storage Account** to manage any media files published to the application, namely images;
* **CosmosDB** as the database system to store and manage data;
* **Azure Functions** to run snippets of serverless code;
* **Azure Cognitive Search** to reduce the complexity of importing data for immediate consumption via indexes.

Additionally, **Redis** was also integrated in the project to increase its overall performance when in comes to response time, while also reducing the amount of processing done by the application and database service.

### 2.1 Auctions App

The auction application component - **auction.project** - includes the backend that corresponds to the project and the test scripts in the format of *yml* files.

The backend was divided into five different modules that interact with one another to allow the use of the services offered by the application. These can be represented as in the following table:

| Module | Role |
| --- | --- |
| **cache** | Communication layer with Redis as the caching system |
| **data** | Data structures and communication layer with CosmosDB as the database system |
| **mgt** | Automated management of Azure resources and corresponding access keys |
| **srv** |  Definition layer for endpoints interacting with the application services |
| **utils** | Storage and availability of auxiliary properties to the application |

The *yml* files use **Artillery** to run different test scenarios and ensure the application behaviour is correct and just. The scenarios make it possible to organize a set of tests defined on data and application endpoints, making the entire process of testing the developed code a little easier.

<div style="page-break-after: always"></div>

### 2.2 Functions App

The second component of the project - **fun.project** - was developed in a Maven project separate to that of the auction project and uses the Azure Functions service to incorporate serverless functions in the application.

Much like the previous component, the functions application
includes a group of modules. However, these modules were reduced to the resources that are imperative to run the functions:

| Module | Role |
| --- | --- |
| **data** | Data structures |
| **dblayer** | Communication layer with CosmosDB as the database system |
| **pt** | Serverless functions |
| **rediscache** |  Communication layer with Redis as the caching system |
| **utils** | Storage and availability of auxiliary properties to the application |

The serverless functions allow the execution of small code snippets that have some utility towards the application, as the following table suggests:

| Function | Trigger | Utility |
| --- | --- | --- |
| **CloseAuctions** | Timer | Automatically closes an auction, at midnight, in case its auction lifetime has passed |
| **Thumbnail** | Blob | Creates a 720p resolution thumbnail of any *jpg* file created or updated to the images container |

Observations:

* **CloseAuctions**
> The data property that determines the end of an auction's lifetime is a date, not including time. This means the function can be triggered daily instead of every couple of seconds, minutes or hours, significantly reducing the amount of queries the database must compute.

* **Thumbnail**
> The thumbnails are stored in a separate thumbnails container and the 720p compression is performed assuming it is the desired resolution for the images used in the application. Only works for *jpg* files.

<div style="page-break-after: always"></div>

## 3. Evaluation

As requested in the project handout, **Artillery** scripts (see Annex [1](#annex1), [2](#annex2) and [3](#annex3)) were configured to test and evaluate the application performance under different settings.

<a name="evaluation"></a>

Firstly, the application was set to be deployed in the West Europe region, with caching. The response times were the following:

* **create-users.yml**
```
http.response_time:
  min: ......................................................................... 54
  max: ......................................................................... 16118
  median: ...................................................................... 117.9
  p95: ......................................................................... 267.8
  p99: ......................................................................... 459.5
```

* **create-auctions.yml**

```
http.response_time:
  min: ......................................................................... 48
  max: ......................................................................... 1664
  median: ...................................................................... 156
  p95: ......................................................................... 550.1
  p99: ......................................................................... 550.1
```

Secondly, the application was maintained as deployed in the West Europe region, but no caching system was used, and the results were as follows:

* **create-users.yml**

```
http.response_time:
  min: ......................................................................... 44
  max: ......................................................................... 199
  median: ...................................................................... 51.9
  p95: ......................................................................... 87.4
  p99: ......................................................................... 108.9
```

* **create-auctions.yml**

```
http.response_time:
  min: ......................................................................... 56
  max: ......................................................................... 189
  median: ...................................................................... 74.4
  p95: ......................................................................... 169
  p99: ......................................................................... 169
```

Unfortunately and due to unforeseen errors, the last setting, that would see the application being deployed to another region other than Europe with or without cache, could not be entirely conceived in time.

In conclusion however, the **median** values show that **not using cache** is the best option in order to achieve the best performance. We are uncertain how this holds true, given that the Redis caching system is supposed to help reduce latency and improve throughput data access, effectively providing better availability and response times. For reasons unknown, the application performance can be better when using cache, but becomes inconsistent and causes timeouts when replying to client requests. This incident may have something to do with the cache implementation.

<div style="page-break-after: always"></div>

## Annexes

Annex 1 - create-users.yml <a name="annex1"></a> (Back to [Evaluation](#evaluation))
```yml
config:
  target: 'https://appwesteurope56773.azurewebsites.net/rest'
  http:
    timeout: 30
  plugins:
    metrics-by-endpoint:
      useOnlyRequestNames: true  # new mode to aggregate metrics in artillery
  processor: "./test-utils.js"
  variables:
    numUsers : 100
  phases:
  - name: "Create users"    # Create users
    duration: 1
    arrivalCount: 1
scenarios:
  - name: 'Create users'
    weight: 1
    flow:
      - loop:                            # let's create 100 users - loop ... count
        - put:                          # First: put image for the user
            url: "/media"
            name: "PUT:/media"
            headers:
              Content-Type: application/octet-stream
              Accept: application/json
            beforeRequest: "uploadImageBody"
            capture: 
              regexp: "(.+)"
              as: "imageId"              # capture the reply as image id to be used in user creation
        - function: "genNewUser"         # Generate the needed information for the user
        - put:
            url: "/user"
            name: "PUT:/user"
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

<div style="page-break-after: always"></div>

Annex 2 - create-auctions.yml <a name="annex2"></a> (Back to [Evaluation](#evaluation))
```yml
config:
  target: 'https://appwesteurope56773.azurewebsites.net/rest'
  http:
    timeout: 10
  plugins:
    metrics-by-endpoint:
      useOnlyRequestNames: true  # new mode to aggregate metrics in artillery
  processor: "./test-utils.js"
  variables:
    numAuctions : 300
    maxBids : 10  # maximum number of bids to generate for each new auction
    maxQuestions : 2 # maximum number of questions to generate for each new auction
  phases:
  - name: "Create auctions"    # Create channels
    duration: 1
    arrivalCount: 1
scenarios:
  - name: 'Create auctions'
    weight: 1
    flow:
      - loop:                            # let's create numAuctions auctions - loop ... count
        - function: "selectUserSkewed"
        - log: "{{ user }}"
        - log: "{{ pwd }}"
        - post:                          # First: login as a user
            url: "/user/auth"
            name: "POST:/user/auth"
            headers:
              Content-Type: application/json
            json:
              userId: "{{ user }}"
              pwd: "{{ pwd }}"
        - function: "genNewAuction"
        - put:                          # First: post image for the auction
            url: "/media"
            name: "PUT:/media"
            headers:
              Content-Type: application/octet-stream
              Accept: application/json
            beforeRequest: "uploadImageBody"
            capture: 
              regexp: "(.+)"
              as: "imageId"              # capture the reply as image id to be used in uction creation
        - post:                          # Create auction
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
              imageId: "{{ imageId }}"
              endTime: "{{ endTime }}"
              minimumPrice: "{{ minimumPrice }}"
              status: "{{ status }}"
            capture:                     # Capturing auction id and store it in variable auctionId
              - json: $.id
                as: "auctionId"
              - json: $.owner
                as: "auctionUser"
        - loop:                          # Let's add numBids bids to the auction
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
          - post:                          # New bid     
              url: "/auction/{{ auctionId }}/bid"
              name: "POST:/auction/*/bid"
              headers:
                Content-Type: application/json
                Accept: application/json
              json:
                id: ""
                auctionId: "{{ auctionId }}"
                user: "{{ user }}"
                value: "{{ value }}"
          count: "{{ numBids }}"   
        - loop:                          # Let's add numQuestions questions to the auction
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
          - post:                          # New question     
              url: "/auction/{{ auctionId }}/question"
              name: "POST:/auction/*/question"
              headers:
                Content-Type: application/json
                Accept: application/json
              json:
                id: ""
                auctionId: "{{ auctionId }}"
                user: "{{ user }}"
                text: "{{ text }}"
              capture:                     # Capturing question id and store it in variable questionId
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
          - put:                          # New reply
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

<div style="page-break-after: always"></div>

Annex 3 - test-utils.js <a name="annex3"></a> (Back to [Evaluation](#evaluation))
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
var imagesIds = []
var images = []
var users = []
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
 * Process reply of the download of an image. 
 * Update the next image to read.
 */
function processUploadReply(requestParams, response, context, ee, next) {
	if( typeof response.body !== 'undefined' && response.body.length > 0) {
		imagesIds.push(response.body)
	}
    return next()
}
/**
 * Select an image to download.
 */
function selectImageToDownload(context, events, done) {
	if( imagesIds.length > 0) {
		context.vars.imageId = imagesIds.sample()
	} else {
		delete context.vars.imageId
	}
	return done()
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
	return done()
}
/**
 * Process reply for of new users to store the id on file
 */
function genNewUserReply(requestParams, response, context, ee, next) {
	if( response.statusCode >= 200 && response.statusCode < 300 && response.body.length > 0)  {
		let u = JSON.parse( response.body)
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
 * Generate data for a new channel
 * Besides the variables for the auction, initializes the following vars:
 * numBids - number of bids to create, if batch creating 
 * numQuestions - number of questions to create, if batch creating 
 * bidValue - price for the next bid
 */
function genNewAuction(context, events, done) {
	context.vars.title = `${Faker.commerce.productName()}`
	context.vars.description = `${Faker.commerce.productDescription()}`
	context.vars.minimumPrice = `${Faker.commerce.price()}`
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
			context.vars.bidValue = context.vars.minimumPrice + random(3)
		}
	}
	context.vars.value = context.vars.bidValue;
	context.vars.bidValue = context.vars.bidValue + 1 + random(3)
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
/**
 * Decide next action
 * 0 -> browse popular
 * 1 -> browse recent
 */
function decideNextAction(context, events, done) {
	delete context.vars.auctionId;
	let rnd = Math.random()
	if( rnd < 0.075)
		context.vars.nextAction = 0; // browsing recent
	else if( rnd < 0.15)
		context.vars.nextAction = 1; // browsing popular
	else if( rnd < 0.225)
		context.vars.nextAction = 2; // browsing user
	else if( rnd < 0.3)
		context.vars.nextAction = 3; // create an auction
	else if( rnd < 0.8)
		context.vars.nextAction = 4; // checking auction
	else if( rnd < 0.95)
		context.vars.nextAction = 5; // do a bid
	else
		context.vars.nextAction = 6; // post a message
	if( context.vars.nextAction == 2) {
		if( Math.random() < 0.5)
			context.vars.user2 = context.vars.user
		else {
			let user = users.sample()
			context.vars.user2 = user.id
		}
	}
	if( context.vars.nextAction == 3) {
		context.vars.title = `${Faker.commerce.productName()}`
		context.vars.description = `${Faker.commerce.productDescription()}`
		context.vars.minimumPrice = `${Faker.commerce.price()}`
		context.vars.bidValue = context.vars.minimumPrice + random(3)
		var d = new Date();
		d.setTime(Date.now() + 60000 + random( 300000));
		context.vars.endTime = d.toISOString();
		context.vars.status = "OPEN";
	}
	if( context.vars.nextAction >= 4) {
		let r = random(3)
		var auct = null
		if( r == 2 && typeof context.vars.auctionsLst == 'undefined')
			r = 1;
		if( r == 2)
  			auct = context.vars.auctionsLst.sample();
		else if( r == 1)
  			auct = context.vars.recentLst.sample();
		else if( r == 0)
  			auct = context.vars.popularLst.sample();
		if( auct == null) {
			return decideNextAction(context,events,done);
		}
		context.vars.auctionId = auct.id
		context.vars.imageId = auct.imageId
	}
	if( context.vars.nextAction == 5)
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
 * Return true with probability 50% 
 */
function random80(context, next) {
  const continueLooping = Math.random() < 0.8
  return next(continueLooping);
}
/**
 * Process reply for of new users to store the id on file
 */
function extractCookie(requestParams, response, context, ee, next) {
	if( response.statusCode >= 200 && response.statusCode < 300)  {
		for( let header of response.rawHeaders) {
			if( header.startsWith("scc:session")) {
				context.vars.mycookie = header.split(';')[0];
			}
		}
	}
    return next()
}
```