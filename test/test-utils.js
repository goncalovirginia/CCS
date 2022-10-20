'use strict';

/***
 * Exported functions to be used in the testing scripts.
 */
module.exports = {
  	uploadImageBody,
	processUploadReply,
	selectImageToDownload,
 	genNewUser,
  	genNewUserReply,
	genNewAuction,
	genNewAuctionReply
}

const Faker = require('faker');
const fs = require('fs');

let imagesIds = []
let images = []
let users = []
let auctions = []

// All endpoints starting with the following prefixes will be aggregated in the same for the statistics
let statsPrefix = [ ["/rest/media/","GET"],
			["/rest/media","PUT"],
			["/rest/user/","GET"],
	]

// Function used to compress statistics
global.myProcessEndpoint = function( str, method) {
	for(let i = 0; i < statsPrefix.length; i++) {
		if( str.startsWith( statsPrefix[i][0]) && method == statsPrefix[i][1])
			return method + ":" + statsPrefix[i][0];
	}
	return method + ":" + str;
}

// Auxiliary function to select an element from an array
Array.prototype.sample = function(){
	   return this[Math.floor(Math.random()*this.length)]
}

// Returns a random value, from 0 to val
function random( val){
	return Math.floor(Math.random() * val)
}

// Loads data about images from disk
function loadData() {
	let basefile = fs.existsSync('/images') ? '/images/cats.' : 'images/cats.';

	for(let i = 1; i <= 40 ; i++) {
		images.push(fs.readFileSync(basefile + i + '.jpeg'))
	}

	if(fs.existsSync('users.data')) {
		users = JSON.parse(fs.readFileSync('users.data','utf8'))
	} 

	if(fs.existsSync('auctions.data')) {
		auctions = JSON.parse(fs.readFileSync('auctions.data','utf8'))
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
 * Select a user.
 */
function selectUser(context, events, done) {
	if( userIds.length > 0) {
		context.vars.userId = userIds.sample()
	} else {
		delete context.vars.userId
	}
	return done()
}

/**
 * Generate data for a new user using Faker
 */
function genNewUser(context, events, done) {
	const first = Faker.name.firstName()
	const last = Faker.name.lastName()
	context.vars.id = first + "." + last
	context.vars.name = first + " " + last
	context.vars.pwd = Faker.internet.password()
	return done()
}

/**
 * Process reply for new users to store the id on file
 */
function genNewUserReply(requestParams, response, context, ee, next) {
	if( response.statusCode >= 200 && response.statusCode < 300 && response.body.length > 0)  {
		let u = JSON.parse( response.body)
		users.push(u)
		fs.writeFileSync('users.data', JSON.stringify(users));
	}
    return next()
}

function genNewAuction(context, events, done) {
	context.vars.title = Faker.vehicle.vehicle()
	context.vars.description = Faker.lorem.paragraph()
	context.vars.photoId = imagesIds.sample()
	context.vars.owner = users.sample().title
	context.vars.endTime = Faker.date.past()
	context.vars.status = Faker.phone.phoneNumber()
	context.vars.minPrice = Faker.datatype.number()
	return done();
}

/**
 * Process reply for new auctions to store the id on file
 */
function genNewAuctionReply(requestParams, response, context, ee, next) {
	if( response.statusCode >= 200 && response.statusCode < 300 && response.body.length > 0)  {
		let a = JSON.parse( response.body)
		auctions.push(a)
		fs.writeFileSync('auctions.data', JSON.stringify(auctions));
	}
	return next()
}



