package scc.srv;

import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import scc.cache.RedisLayer;
import scc.data.*;
import jakarta.ws.rs.core.Cookie;

import java.util.Arrays;
import java.util.List;

@Path("/auction")
public class AuctionResource extends AccessControl{
	
	private final CosmosDBLayer db = CosmosDBLayer.getInstance();
	
	@Context
	ResourceContext resourceContext;
	
	@PUT
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Auction createAuction(@CookieParam("scc:session") Cookie session, Auction auction) {
		try {
			validateAuction(auction);
			checkSessionCookie(session, auction.getOwner());
			resourceContext.getResource(UserResource.class).getUser(auction.getOwner());
			resourceContext.getResource(MediaResource.class).fileExists(auction.getPhotoId());
			Auction dbAuction = new Auction(db.putAuction(new AuctionDAO(auction)).getItem());
			return RedisLayer.putAuction(dbAuction);
		} catch(WebApplicationException e) {
			throw e;
		} catch(Exception e) {
			throw new InternalServerErrorException( e);
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
	
	@POST
	@Path("/{id}/bid")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Bid bid(@CookieParam("scc:session") Cookie session, @PathParam("id") String id, Bid bid) {
		validateBid(bid);
		checkSessionCookie(session, bid.getUser());
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
	public Question postQuestion(@CookieParam("scc:session") Cookie session, @PathParam("id") String id, Question question) {
		validateQuestion(question);
		if(question.getAnswer() != null || question.getAnswer() != ""){
			throw new NotAuthorizedException("You cant create a question with an answer already");
		}
		checkSessionCookie(session, question.getUser());
		getAuction(id);
		resourceContext.getResource(UserResource.class).getUser(question.getUser());
		return new Question(db.putQuestion(new QuestionDAO(question)).getItem());
	}


	@PUT
	@Path("/{id}/question")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Question putQuestion(@CookieParam("scc:session") Cookie session, @PathParam("id") String id, Question question) {
		validateQuestion(question);
		checkSessionCookie(session, question.getUser());
		getAuction(id);
		Session s = RedisLayer.getSession(session.getValue());
		if(s == null){
			s = new Session(CosmosDBLayer.getInstance().getSession(session.getValue()));
		}
		AuctionDAO auction = db.getAuctionsByOwnerAndName(s.getUser(), question.getAuction());
	
		if(s.getUser() != new Auction(auction).getOwner() && question.getAnswer() != ""){
			throw new NotAuthorizedException("Only the owner of the auction can answer the question!");
		}

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


	public void validateAuction(Auction auction){

		List<String> list = Arrays.asList(new String[]{"", null, "closed", "open"});

		if(list.contains(auction.getId())){
			throw new IllegalArgumentException("Auction id must not be empty!");
		}

		if(list.contains(auction.getTitle())){
			throw new IllegalArgumentException("Auction title must not be empty!");
		}

		if(list.contains(auction.getPhotoId())){
			throw new IllegalArgumentException("Auction photo must not be empty!");
		}

		if(list.contains(auction.getEndTime())){
			throw new IllegalArgumentException("Auction end time must not be empty!");
		}

		if(!list.contains(auction.getStatus()) && !list.contains(auction.getStatus())){
			throw new IllegalArgumentException("Auction status must be 'open' or 'closed'");
		}
		
		if(auction.getMinPrice() < 1){
			throw new IllegalArgumentException("Auction minimum price must not be lower than 1!");
		}

	}
	
	public void validateBid(Bid bid){

		List<String> list = Arrays.asList("", null);

		if(list.contains(bid.getId())){
			throw new IllegalArgumentException("Bid id must not be empty!");
		}

		if(list.contains(bid.getAuction())){
			throw new IllegalArgumentException("Bid action name must not be empty!");
		}

		if(list.contains(bid.getUser())){
			throw new IllegalArgumentException("Bid username must not be empty!");
		}

		if(bid.getAmount() < 1){
			throw new IllegalArgumentException("Bid amount must not be lower than 1!");
		}

	}

	
	public void validateQuestion(Question question){

		List<String> list = Arrays.asList(new String[]{"", null});

		if(list.contains(question.getId())){
			throw new IllegalArgumentException("Question id must not be empty!");
		}

		if(list.contains(question.getAuction())){
			throw new IllegalArgumentException("Question action name must not be empty!");
		}

		if(list.contains(question.getUser())){
			throw new IllegalArgumentException("Question username must not be empty!");
		}

		if(list.contains(question.getText())){
			throw new IllegalArgumentException("Question text must not be empty!");
		}

	}
	
}
