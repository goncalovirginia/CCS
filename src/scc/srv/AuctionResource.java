package scc.srv;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import scc.data.*;

import java.util.List;

@Path("/auction")
public class AuctionResource {
	
	private final CosmosDBLayer auctions = CosmosDBLayer.getInstance();
	
	@PUT
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Auction createAuction(Auction auction) {
		return new Auction(auctions.putAuction(new AuctionDAO(auction)).getItem());
	}
	
	@PUT
	@Path("/{id}/bid")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Bid bid(Bid bid) {
		return new Bid(auctions.putBid(new BidDAO(bid)).getItem());
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
	public Question postQuestion(Question question) {
		return new Question(auctions.putQuestion(new QuestionDAO(question)).getItem());
	}
	
	@GET
	@Path("/{id}/question")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Question> listQuestions(@PathParam("id") String id) {
		return auctions.getQuestions(id).stream().map(Question::new).toList();
	}
	
}
