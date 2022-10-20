package scc.srv;

import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import scc.cache.RedisCache;
import scc.data.*;

import java.util.List;

@Path("/auction")
public class AuctionResource {
	
	private static final String AUCTIONS = "auctions";
	private final CosmosDBLayer db = CosmosDBLayer.getInstance();
	
	@Context
	ResourceContext resourceContext;
	
	@PUT
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Auction createAuction(Auction auction) {
		resourceContext.getResource(UserResource.class).getUser(auction.getOwner());
		resourceContext.getResource(MediaResource.class).fileExists(auction.getPhotoId());
		Auction dbAuction = new Auction(db.putAuction(new AuctionDAO(auction)).getItem());
		return RedisCache.writeToHashmap(AUCTIONS, dbAuction.getTitle(), dbAuction);
	}
	
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Auction getAuction(@PathParam("id") String id) {
		Auction cacheResult = RedisCache.readFromHashmap(AUCTIONS, id, Auction.class);
		return cacheResult != null ? cacheResult :
				RedisCache.writeToHashmap(AUCTIONS, id, new Auction(db.getAuctionByTitle(id)));
	}
	
	@PUT
	@Path("/{id}/bid")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Bid bid(@PathParam("id") String id, Bid bid) {
		getAuction(id);
		resourceContext.getResource(UserResource.class).getUser(bid.getUser());
		return new Bid(db.putBid(new BidDAO(bid)).getItem());
	}
	
	@GET
	@Path("/{id}/bid")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Bid> listBids(@PathParam("id") String id) {
		getAuction(id);
		return db.getBids(id).stream().map(Bid::new).toList();
	}
	
	@POST
	@Path("/{id}/question")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Question postQuestion(@PathParam("id") String id, Question question) {
		getAuction(id);
		resourceContext.getResource(UserResource.class).getUser(question.getUser());
		return new Question(db.putQuestion(new QuestionDAO(question)).getItem());
	}
	
	@GET
	@Path("/{id}/question")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Question> listQuestions(@PathParam("id") String id) {
		getAuction(id);
		return db.getQuestions(id).stream().map(Question::new).toList();
	}
	
}
