package scc.srv;

import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import scc.cache.RedisLayer;
import scc.data.*;
import jakarta.ws.rs.core.Cookie;

import java.util.List;

@Path("/auction")
public class AuctionResource extends AccessControl{
	
	private final CosmosDBLayer db = CosmosDBLayer.getInstance();
	
	@Context
	ResourceContext resourceContext;
	
	@PUT
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Auction createAuction(@CookieParam("scc:session") Cookie session, Auction auction) {
		try {
			checkSessionCookie(session, auction.getOwner());
			resourceContext.getResource(UserResource.class).getUser(auction.getOwner());
			resourceContext.getResource(MediaResource.class).fileExists(auction.getPhotoId());
			Auction dbAuction = new Auction(db.putAuction(new AuctionDAO(auction)).getItem());
			return RedisLayer.putAuction(dbAuction);
		} catch( WebApplicationException e) {
			throw e;
		} catch( Exception e) {
			throw new InternalServerErrorException( e);
		}
	}
	
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Auction getAuction(@PathParam("id") String id) {
		Auction cacheResult = RedisLayer.getAuction(id);
		return cacheResult != null ? cacheResult : RedisLayer.putAuction(new Auction(db.getAuctionById(id)));
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
	
	@PUT
	@Path("/{id}/bid")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Bid bid(@CookieParam("scc:session") Cookie session, @PathParam("id") String id, Bid bid) {
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
		checkSessionCookie(session, question.getUser());
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
