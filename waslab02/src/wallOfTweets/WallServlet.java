package wallOfTweets;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.repackaged.org.json.JSONArray;
import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;



@SuppressWarnings("serial")
@WebServlet(urlPatterns = {"/tweets", "/tweets/*"})
public class WallServlet extends HttpServlet {

	private String TWEETS_URI = "/waslab02/tweets/";

	@Override
	// Implements GET http://localhost:8080/waslab02/tweets
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		resp.setContentType("application/json");
		resp.setHeader("Cache-control", "no-cache");
		List<Tweet> tweets= Database.getTweets();
		JSONArray job = new JSONArray();
		for (Tweet t: tweets) {
			JSONObject jt = new JSONObject(t);
			jt.remove("class");
			job.put(jt);
		}
		resp.getWriter().println(job.toString());
	}

	@Override
	// Implements POST http://localhost:8080/waslab02/tweets/:id/likes
	//        and POST http://localhost:8080/waslab02/tweets
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		String uri = req.getRequestURI();
		int lastIndex = uri.lastIndexOf("/likes");
		if (lastIndex > -1) {  // uri ends with "/likes"
			// Implements POST http://localhost:8080/waslab02/tweets/:id/likes
			long id = Long.valueOf(uri.substring(TWEETS_URI.length(),lastIndex));		
			resp.setContentType("text/plain");
			resp.getWriter().println(Database.likeTweet(id));
		}
		else { 
			// Implements POST http://localhost:8080/waslab02/tweets
			int max_length_of_data = req.getContentLength();
			byte[] httpInData = new byte[max_length_of_data];
			ServletInputStream  httpIn  = req.getInputStream();
			httpIn.readLine(httpInData, 0, max_length_of_data);
			String body = new String(httpInData);
			/*      ^ 	
		      The String variable body contains the sent (JSON) Data. 
		      Complete the implementation below.*/
			/* TASK #3 */
			
			try {
				JSONObject tweet_json = new JSONObject(body);
				String author = tweet_json.getString("author");
				String text = tweet_json.getString("text");
				Tweet newTweet = Database.insertTweet(author, text);
				/* TASK #5 */
				
				JSONObject newTweetJSON = new JSONObject(newTweet);
				String encriptedTweet = encriptarTweet(newTweet.getId().toString());
				newTweetJSON.accumulate("token", encriptedTweet);
				System.out.println(newTweetJSON);
				resp.getWriter().println(newTweetJSON.toString());
				
				/* TASK #5 end */
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			/* TASK #3 end */
		}
	}
	
	public boolean existsId(String token) {
		Vector<Tweet> tweets = Database.getTweets();
		for(Tweet tweet : tweets) {
			if(token.equals(encriptarTweet(tweet.getId().toString()))) 
				return true;
		}
		return false;
	}
	
	@Override
	// Implements DELETE http://localhost:8080/waslab02/tweets/:id
	public void doDelete(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		String uri = req.getRequestURI();
		String token = req.getHeader("token");
		int id = uri.lastIndexOf("/likes");
		if(existsId(token))Database.deleteTweet(id);

		//throw new ServletException("DELETE not yet implemented");
	}
	
	private static String encriptarTweet(String tweetString) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(
					tweetString.getBytes(StandardCharsets.UTF_8));
			    StringBuilder hexString = new StringBuilder(2 * hash.length);
			    for (int i = 0; i < hash.length; i++) {
			        String hex = Integer.toHexString(0xff & hash[i]);
			        if(hex.length() == 1) {
			            hexString.append('0');
			        }
			        hexString.append(hex);
			    }
			    return hexString.toString();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			return "";
		}

	}

}
