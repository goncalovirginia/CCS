package scc.srv;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import scc.cache.RedisCache;
import scc.data.*;

import java.util.List;

@Path("/auction")
public class AuctionResource {
	
	private static final String AUCTIONS = "auctions", BIDS = "bids", QUESTIONS = "questions";
	private final CosmosDBLayer auctions = CosmosDBLayer.getInstance();
	
	@PUT
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Auction createAuction(Auction auction) {
		auctions.putAuction(new AuctionDAO(auction));
		Auction dbAuction = getAuction(auction.getTitle());
		return RedisCache.writeToHashmap(AUCTIONS, dbAuction.getTitle(), dbAuction);
	}
	
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Auction getAuction(@PathParam("id") String id) {
		Auction cacheResult = RedisCache.readFromHashmap(AUCTIONS, id, Auction.class);
		return cacheResult != null ? cacheResult :
				RedisCache.writeToHashmap(AUCTIONS, id, new Auction(auctions.getAuctionByTitle(id)));
	}
	
	@PUT
	@Path("/{id}/bid")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Bid bid(@PathParam("id") String id, Bid bid) {
		if (getAuction(id) == null) {
			throw new NotFoundException();
		}
		
		auctions.putBid(new BidDAO(bid));
		
		return bid;
	}
	
	@GET
	@Path("/{id}/bid")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Bid> listBids(@PathParam("id") String id) {
		return auctions.getBids(id).stream().map(Bid::new).toList();
	}
	
	@POST
	@Path("/{id}/question")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Question postQuestion(@PathParam("id") String id, Question question) {
		return new Question(auctions.putQuestion(new QuestionDAO(question)).getItem());
	}
	
	@GET
	@Path("/{id}/question")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Question> listQuestions(@PathParam("id") String id) {
		return auctions.getQuestions(id).stream().map(Question::new).toList();
	}
	
}
