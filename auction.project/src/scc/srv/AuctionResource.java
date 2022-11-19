package scc.srv;

import com.azure.cosmos.implementation.ConflictException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import scc.cache.RedisLayer;
import scc.data.*;
import scc.utils.CognitiveSearchClient;

import java.util.Arrays;
import java.util.List;

@Path("/auction")
public class AuctionResource extends AccessControl {
	
	private final CosmosDBLayer db = CosmosDBLayer.getInstance();
	
	@Context
	ResourceContext resourceContext;
	
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Auction createAuction(@CookieParam("scc:session") Cookie session, Auction auction) {
		try {
			checkSessionCookie(session, auction.getOwner());
			validateAuction(auction);
			return RedisLayer.putAuction(new Auction(db.putAuction(new AuctionDAO(auction)).getItem()));
		}
		catch (ConflictException e) {
			return createAuction(session, auction);
		}
	}
	
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Auction getAuction(@PathParam("id") String id) {
		Auction cacheResult = RedisLayer.getAuction(id);
		return cacheResult != null ? cacheResult : new Auction(db.getAuctionById(id));
	}
	
	@GET
	@Path("/ending")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Auction> getAuctionsEndingSoon() {
		return db.getAuctionsEndingSoon().stream().map(Auction::new).toList();
	}
	
	@GET
	@Path("/owner/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Auction> getAuctionsByOwner(@PathParam("id") String id) {
		return db.getAuctionsByOwner(id).stream().map(Auction::new).toList();
	}
	
	@GET
	@Path("/queryDescription")
	@Produces(MediaType.APPLICATION_JSON)
	public String queryAuctionDescription(@QueryParam("description") String description) {
		return CognitiveSearchClient.queryAuctionDescription(description);
	}
	
	@GET
	@Path("/countPerUser")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Object> getAuctionCountPerUser() {
		return db.getAuctionCountPerUser().stream().toList();
	}
	
	@GET
	@Path("/trending")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Object> getTrendingAuctions() {
		return db.getTrendingAuctions().stream().toList();
	}
	
	@POST
	@Path("/{id}/bid")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Bid bid(@CookieParam("scc:session") Cookie session, @PathParam("id") String id, Bid bid) {
		try {
			checkSessionCookie(session, bid.getUser());
			validateBid(bid);
			resourceContext.getResource(UserResource.class).getUser(bid.getUser());
			return new Bid(db.putBid(new BidDAO(bid)).getItem());
		}
		catch (ConflictException e) {
			return bid(session, id, bid);
		}
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
	public Question postQuestion(@CookieParam("scc:session") Cookie session, @PathParam("id") String id, Question question) {
		
		try {
			checkSessionCookie(session, question.getUser());
			validateQuestion(question);
			return new Question(db.putQuestion(new QuestionDAO(question)).getItem());
		}
		catch (ConflictException e) {
			return postQuestion(session, id, question);
		}
	}
	
	@PUT
	@Path("/{auctionId}/question/{questionId}/reply")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Question reply(@CookieParam("scc:session") Cookie session, @PathParam("auctionId") String auctionId, @PathParam("questionId") String questionId, String reply) {
		if (auctionId == null || auctionId.equals("") || questionId == null || questionId.equals("") || reply == null || reply.equals("")) {
			throw new IllegalArgumentException();
		}
		
		Auction auction = getAuction(auctionId);
		Session s = checkSessionCookie(session, auction.getOwner());
		QuestionDAO question = db.getQuestion(questionId);
		question.setAnswer(reply);
		return new Question(db.putQuestion(question).getItem());
	}
	
	@GET
	@Path("/{id}/question")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Question> listQuestions(@PathParam("id") String id) {
		getAuction(id);
		return db.getQuestions(id).stream().map(Question::new).toList();
	}
	
	
	private void validateAuction(Auction auction) {
		List<String> list = Arrays.asList("", null, "closed", "open");
	
		if (list.contains(auction.getTitle())) 
			throw new IllegalArgumentException("Auction title must not be empty!");
		
		if (list.contains(auction.getPhotoId())) 
			throw new IllegalArgumentException("Auction photo must not be empty!");
		
		if (list.contains(auction.getEndTime())) 
			throw new IllegalArgumentException("Auction end time must not be empty!");
		
		if (!list.contains(auction.getStatus()) && !list.contains(auction.getStatus())) 
			throw new IllegalArgumentException("Auction status must be 'open' or 'closed'");
		
		if (auction.getMinPrice() < 1) 
			throw new IllegalArgumentException("Auction minimum price must not be lower than 1!");
		
		resourceContext.getResource(UserResource.class).getUser(auction.getOwner());
		resourceContext.getResource(MediaResource.class).fileExists(auction.getPhotoId());
	}
	
	private void validateBid(Bid bid) {
		List<String> list = Arrays.asList("", null);
		
		if (list.contains(bid.getAuction()))
			throw new IllegalArgumentException("Bid action name must not be empty!");
		
		if (list.contains(bid.getUser())) 
			throw new IllegalArgumentException("Bid username must not be empty!");
		
		if (bid.getAmount() < 1) 
			throw new IllegalArgumentException("Bid amount must not be lower than 1!");
		
		getAuction(bid.getId());
	}
	
	private void validateQuestion(Question question) {
		List<String> list = Arrays.asList("", null);
		
		if (list.contains(question.getAuction())) 
			throw new IllegalArgumentException("Question action name must not be empty!");
		
		if (list.contains(question.getUser())) 
			throw new IllegalArgumentException("Question username must not be empty!");
		
		if (list.contains(question.getText())) 
			throw new IllegalArgumentException("Question text must not be empty!");
		
		if (list.contains(question.getAnswer()))
			throw new NotAuthorizedException("You cant create a question with an answer already!");

		getAuction(question.getAuction());
		resourceContext.getResource(UserResource.class).getUser(question.getUser());
	}
	
}
